<template>
  <div class="p-8">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-900">{{ $t('actuator.title') }}</h1>
      <button class="btn-primary" @click="openCreate">+ {{ $t('actuator.new') }}</button>
    </div>

    <div v-if="loading" class="text-gray-500">{{ $t('common.loading') }}</div>

    <div v-else class="card overflow-hidden p-0">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b border-gray-200">
          <tr>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('common.name') }}</th>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('common.type') }}</th>
            <th class="text-left px-6 py-3 font-medium text-gray-600">Zone</th>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('actuator.state') }}</th>
            <th class="text-right px-6 py-3 font-medium text-gray-600">{{ $t('common.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in actuators" :key="a.id" class="border-b border-gray-100 hover:bg-gray-50">
            <td class="px-6 py-4 font-medium">{{ a.name }}</td>
            <td class="px-6 py-4">{{ $t(`actuator.types.${a.type}`) }}</td>
            <td class="px-6 py-4 text-gray-500">{{ a.zoneName }}</td>
            <td class="px-6 py-4">
              <button
                @click="toggleState(a)"
                :class="a.state === 'ENCENDIDO'
                  ? 'bg-green-100 text-green-700 hover:bg-green-200'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
                class="px-3 py-1 rounded-full text-xs font-medium transition-colors"
              >
                {{ $t(`actuator.states.${a.state}`) }}
              </button>
            </td>
            <td class="px-6 py-4 text-right space-x-2">
              <button class="btn-secondary text-xs" @click="openEdit(a)">{{ $t('common.edit') }}</button>
              <button class="btn-danger text-xs" @click="remove(a)">{{ $t('common.delete') }}</button>
            </td>
          </tr>
          <tr v-if="!actuators.length">
            <td colspan="5" class="px-6 py-8 text-center text-gray-400">{{ $t('common.noData') }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-white rounded-2xl shadow-2xl p-6 w-full max-w-md">
        <h2 class="text-lg font-bold mb-4">{{ editingItem ? $t('actuator.edit') : $t('actuator.new') }}</h2>
        <form @submit.prevent="save">
          <div class="space-y-4">
            <div>
              <label class="form-label">{{ $t('common.name') }} *</label>
              <input v-model="form.name" class="form-input" required />
            </div>
            <div>
              <label class="form-label">{{ $t('common.type') }} *</label>
              <select v-model="form.type" class="form-input" required>
                <option v-for="t in actuatorTypes" :key="t" :value="t">{{ $t(`actuator.types.${t}`) }}</option>
              </select>
            </div>
            <div>
              <label class="form-label">Zone *</label>
              <select v-model="form.zoneId" class="form-input" required>
                <option v-for="z in zones" :key="z.id" :value="z.id">{{ z.name }}</option>
              </select>
            </div>
          </div>
          <div class="flex justify-end gap-3 mt-6">
            <button type="button" class="btn-secondary" @click="showModal = false">{{ $t('common.cancel') }}</button>
            <button type="submit" class="btn-primary">{{ $t('common.save') }}</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { actuatorService } from '@/services/actuatorService'
import { zoneService } from '@/services/zoneService'

const actuators = ref([])
const zones = ref([])
const loading = ref(true)
const showModal = ref(false)
const editingItem = ref(null)
const form = ref({ name: '', type: 'RIEGO', zoneId: null })

const actuatorTypes = ['RIEGO', 'VENTILACION', 'ILUMINACION', 'CALEFACCION']

onMounted(async () => {
  [actuators.value, zones.value] = await Promise.all([actuatorService.getAll(), zoneService.getAll()])
  loading.value = false
})

function openCreate() {
  editingItem.value = null
  form.value = { name: '', type: 'RIEGO', zoneId: zones.value[0]?.id || null }
  showModal.value = true
}

function openEdit(item) {
  editingItem.value = item
  form.value = { name: item.name, type: item.type, zoneId: item.zoneId }
  showModal.value = true
}

async function save() {
  if (editingItem.value) {
    const updated = await actuatorService.update(editingItem.value.id, form.value)
    const idx = actuators.value.findIndex(a => a.id === editingItem.value.id)
    if (idx !== -1) actuators.value[idx] = updated
  } else {
    actuators.value.push(await actuatorService.create(form.value))
  }
  showModal.value = false
}

async function toggleState(item) {
  const newState = item.state === 'ENCENDIDO' ? 'APAGADO' : 'ENCENDIDO'
  const updated = await actuatorService.setState(item.id, newState)
  const idx = actuators.value.findIndex(a => a.id === item.id)
  if (idx !== -1) actuators.value[idx] = updated
}

async function remove(item) {
  if (confirm(`Delete "${item.name}"?`)) {
    await actuatorService.remove(item.id)
    actuators.value = actuators.value.filter(a => a.id !== item.id)
  }
}
</script>
