<template>
  <div class="p-8">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-900">{{ $t('zone.title') }}</h1>
      <button class="btn-primary" @click="openCreate">+ {{ $t('zone.new') }}</button>
    </div>

    <div v-if="loading" class="text-gray-500">{{ $t('common.loading') }}</div>

    <div v-else class="card overflow-hidden p-0">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b border-gray-200">
          <tr>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('common.name') }}</th>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('zone.greenhouse') }}</th>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('common.description') }}</th>
            <th class="text-right px-6 py-3 font-medium text-gray-600">{{ $t('common.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="z in zones" :key="z.id" class="border-b border-gray-100 hover:bg-gray-50">
            <td class="px-6 py-4 font-medium">{{ z.name }}</td>
            <td class="px-6 py-4 text-gray-500">{{ z.greenhouseName }}</td>
            <td class="px-6 py-4 text-gray-500 truncate max-w-xs">{{ z.description || '—' }}</td>
            <td class="px-6 py-4 text-right space-x-2">
              <button class="btn-secondary text-xs" @click="openEdit(z)">{{ $t('common.edit') }}</button>
              <button class="btn-danger text-xs"    @click="remove(z)">{{ $t('common.delete') }}</button>
            </td>
          </tr>
          <tr v-if="!zones.length">
            <td colspan="4" class="px-6 py-8 text-center text-gray-400">{{ $t('common.noData') }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-white rounded-2xl shadow-2xl p-6 w-full max-w-md">
        <h2 class="text-lg font-bold mb-4">{{ editingItem ? $t('zone.edit') : $t('zone.new') }}</h2>
        <form @submit.prevent="save">
          <div class="space-y-4">
            <div>
              <label class="form-label">{{ $t('common.name') }} *</label>
              <input v-model="form.name" class="form-input" required />
            </div>
            <div>
              <label class="form-label">{{ $t('zone.greenhouse') }} *</label>
              <select v-model="form.greenhouseId" class="form-input" required>
                <option v-for="g in greenhouses" :key="g.id" :value="g.id">{{ g.name }}</option>
              </select>
            </div>
            <div>
              <label class="form-label">{{ $t('common.description') }}</label>
              <textarea v-model="form.description" class="form-input" rows="3" />
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
import { useI18n } from 'vue-i18n'
import { zoneService } from '@/services/zoneService'
import { greenhouseService } from '@/services/greenhouseService'

const { t } = useI18n()
const zones = ref([])
const greenhouses = ref([])
const loading = ref(true)
const showModal = ref(false)
const editingItem = ref(null)
const form = ref({ name: '', greenhouseId: null, description: '' })

onMounted(async () => {
  [zones.value, greenhouses.value] = await Promise.all([
    zoneService.getAll(),
    greenhouseService.getAll()
  ])
  loading.value = false
})

function openCreate() {
  editingItem.value = null
  form.value = { name: '', greenhouseId: greenhouses.value[0]?.id || null, description: '' }
  showModal.value = true
}

function openEdit(item) {
  editingItem.value = item
  form.value = { name: item.name, greenhouseId: item.greenhouseId, description: item.description || '' }
  showModal.value = true
}

async function save() {
  if (editingItem.value) {
    const updated = await zoneService.update(editingItem.value.id, form.value)
    const idx = zones.value.findIndex(z => z.id === editingItem.value.id)
    if (idx !== -1) zones.value[idx] = updated
  } else {
    zones.value.push(await zoneService.create(form.value))
  }
  showModal.value = false
}

async function remove(item) {
  if (confirm(t('zone.deleteConfirm'))) {
    await zoneService.remove(item.id)
    zones.value = zones.value.filter(z => z.id !== item.id)
  }
}
</script>
