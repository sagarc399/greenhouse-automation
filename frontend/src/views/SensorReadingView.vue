<template>
  <div class="p-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-6">{{ $t('reading.title') }}</h1>

    <div class="flex gap-4 mb-6">
      <div class="flex-1">
        <label class="form-label">{{ $t('reading.selectSensor') }}</label>
        <select v-model="selectedSensorId" @change="loadReadings" class="form-input max-w-xs">
          <option :value="null">— {{ $t('reading.selectSensor') }} —</option>
          <option v-for="s in sensors" :key="s.id" :value="s.id">
            {{ s.name }} ({{ s.zoneName }})
          </option>
        </select>
      </div>
      <div class="flex items-end">
        <button class="btn-primary" @click="openCreate" :disabled="!selectedSensorId">
          + {{ $t('reading.new') }}
        </button>
      </div>
    </div>

    <div v-if="loading" class="text-gray-500">{{ $t('common.loading') }}</div>

    <div v-else-if="selectedSensorId" class="card overflow-hidden p-0">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b border-gray-200">
          <tr>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('reading.value') }}</th>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('reading.unit') }}</th>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('reading.recordedAt') }}</th>
            <th class="text-right px-6 py-3 font-medium text-gray-600">{{ $t('common.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in readings" :key="r.id" class="border-b border-gray-100 hover:bg-gray-50">
            <td class="px-6 py-4 font-medium text-lg">{{ r.value }}</td>
            <td class="px-6 py-4 text-gray-500">{{ r.unit || '—' }}</td>
            <td class="px-6 py-4 text-gray-500">{{ formatDate(r.recordedAt) }}</td>
            <td class="px-6 py-4 text-right">
              <button class="btn-danger text-xs" @click="remove(r)">{{ $t('common.delete') }}</button>
            </td>
          </tr>
          <tr v-if="!readings.length">
            <td colspan="4" class="px-6 py-8 text-center text-gray-400">{{ $t('common.noData') }}</td>
          </tr>
        </tbody>
      </table>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="flex items-center justify-between px-6 py-3 border-t border-gray-100">
        <span class="text-xs text-gray-500">
          {{ $t('reading.recordedAt') }}: {{ totalElements }}
        </span>
        <div class="flex gap-2">
          <button
            class="btn-secondary text-xs py-1 px-3"
            :disabled="currentPage === 0"
            @click="goToPage(currentPage - 1)"
          >‹</button>
          <span class="text-xs text-gray-600 py-1 px-2">{{ currentPage + 1 }} / {{ totalPages }}</span>
          <button
            class="btn-secondary text-xs py-1 px-3"
            :disabled="currentPage >= totalPages - 1"
            @click="goToPage(currentPage + 1)"
          >›</button>
        </div>
      </div>
    </div>

    <!-- Create modal -->
    <div v-if="showModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-white rounded-2xl shadow-2xl p-6 w-full max-w-md">
        <h2 class="text-lg font-bold mb-4">{{ $t('reading.new') }}</h2>
        <form @submit.prevent="save">
          <div class="space-y-4">
            <div>
              <label class="form-label">{{ $t('reading.value') }} *</label>
              <input v-model.number="form.value" type="number" step="0.01" class="form-input" required />
            </div>
            <div>
              <label class="form-label">{{ $t('reading.unit') }}</label>
              <input v-model="form.unit" class="form-input" placeholder="°C, %, lux, pH…" />
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
import { sensorService } from '@/services/sensorService'
import { sensorReadingService } from '@/services/sensorReadingService'

const { t } = useI18n()
const sensors = ref([])
const readings = ref([])
const selectedSensorId = ref(null)
const loading = ref(false)
const showModal = ref(false)
const form = ref({ value: 0, unit: '' })

// Pagination state
const currentPage = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)
const PAGE_SIZE = 20

onMounted(async () => {
  sensors.value = await sensorService.getAll()
})

async function loadReadings() {
  if (!selectedSensorId.value) return
  currentPage.value = 0
  await fetchPage(0)
}

async function fetchPage(page) {
  loading.value = true
  const result = await sensorReadingService.getBySensor(selectedSensorId.value, page, PAGE_SIZE)
  readings.value     = result.content      || []
  totalPages.value   = result.totalPages   ?? 0
  totalElements.value = result.totalElements ?? 0
  currentPage.value  = result.number        ?? 0
  loading.value = false
}

async function goToPage(page) {
  await fetchPage(page)
}

function openCreate() {
  form.value = { value: 0, unit: '' }
  showModal.value = true
}

async function save() {
  await sensorReadingService.create({ ...form.value, sensorId: selectedSensorId.value })
  showModal.value = false
  await fetchPage(currentPage.value)
}

async function remove(item) {
  if (confirm(t('reading.deleteConfirm'))) {
    await sensorReadingService.remove(item.id)
    readings.value = readings.value.filter(r => r.id !== item.id)
  }
}

function formatDate(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleString()
}
</script>
