<template>
  <div class="p-8">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-900">{{ $t('sensor.title') }}</h1>
      <button class="btn-primary" @click="openCreate">+ {{ $t('sensor.new') }}</button>
    </div>

    <div v-if="loading" class="text-gray-500">{{ $t('common.loading') }}</div>

    <div v-else class="card overflow-hidden p-0">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b border-gray-200">
          <tr>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('common.name') }}</th>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('common.type') }}</th>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('sensor.zone') }}</th>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('sensor.active') }}</th>
            <th class="text-right px-6 py-3 font-medium text-gray-600">{{ $t('common.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in sensors" :key="s.id" class="border-b border-gray-100 hover:bg-gray-50">
            <td class="px-6 py-4 font-medium">{{ s.name }}</td>
            <td class="px-6 py-4">{{ $t(`sensor.types.${s.type}`) }}</td>
            <td class="px-6 py-4 text-gray-500">{{ s.zoneName }}</td>
            <td class="px-6 py-4">
              <span :class="s.active ? 'text-green-600' : 'text-red-500'">
                {{ s.active ? '✓' : '✗' }}
              </span>
            </td>
            <td class="px-6 py-4 text-right space-x-2">
              <button class="btn-secondary text-xs" @click="openEdit(s)">{{ $t('common.edit') }}</button>
              <button class="btn-danger text-xs" @click="remove(s)">{{ $t('common.delete') }}</button>
            </td>
          </tr>
          <tr v-if="!sensors.length">
            <td colspan="5" class="px-6 py-8 text-center text-gray-400">{{ $t('common.noData') }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-white rounded-2xl shadow-2xl p-6 w-full max-w-md">
        <h2 class="text-lg font-bold mb-4">{{ editingItem ? $t('sensor.edit') : $t('sensor.new') }}</h2>
        <form @submit.prevent="save">
          <div class="space-y-4">
            <div>
              <label class="form-label">{{ $t('common.name') }} *</label>
              <input v-model="form.name" class="form-input" required />
            </div>
            <div>
              <label class="form-label">{{ $t('common.type') }} *</label>
              <select v-model="form.type" class="form-input" required>
                <option v-for="t in sensorTypes" :key="t" :value="t">{{ $t(`sensor.types.${t}`) }}</option>
              </select>
            </div>
            <div>
              <label class="form-label">{{ $t('sensor.zone') }} *</label>
              <select v-model="form.zoneId" class="form-input" required>
                <option v-for="z in zones" :key="z.id" :value="z.id">{{ z.name }}</option>
              </select>
            </div>
            <div>
              <label class="form-label">{{ $t('sensor.model') }}</label>
              <input v-model="form.model" class="form-input" />
            </div>
            <div class="flex items-center gap-2">
              <input type="checkbox" v-model="form.active" id="active" />
              <label for="active" class="text-sm text-gray-700">{{ $t('sensor.active') }}</label>
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
import { sensorService } from '@/services/sensorService'
import { zoneService } from '@/services/zoneService'

const sensors = ref([])
const zones = ref([])
const loading = ref(true)
const showModal = ref(false)
const editingItem = ref(null)
const form = ref({ name: '', type: 'TEMPERATURA', zoneId: null, model: '', active: true })

const sensorTypes = ['TEMPERATURA', 'HUMEDAD_AMBIENTAL', 'HUMEDAD_SUELO', 'LUZ', 'PH']

onMounted(async () => {
  [sensors.value, zones.value] = await Promise.all([sensorService.getAll(), zoneService.getAll()])
  loading.value = false
})

function openCreate() {
  editingItem.value = null
  form.value = { name: '', type: 'TEMPERATURA', zoneId: zones.value[0]?.id || null, model: '', active: true }
  showModal.value = true
}

function openEdit(item) {
  editingItem.value = item
  form.value = { name: item.name, type: item.type, zoneId: item.zoneId, model: item.model || '', active: item.active }
  showModal.value = true
}

async function save() {
  if (editingItem.value) {
    const updated = await sensorService.update(editingItem.value.id, form.value)
    const idx = sensors.value.findIndex(s => s.id === editingItem.value.id)
    if (idx !== -1) sensors.value[idx] = updated
  } else {
    sensors.value.push(await sensorService.create(form.value))
  }
  showModal.value = false
}

async function remove(item) {
  if (confirm(`Delete "${item.name}"?`)) {
    await sensorService.remove(item.id)
    sensors.value = sensors.value.filter(s => s.id !== item.id)
  }
}
</script>
