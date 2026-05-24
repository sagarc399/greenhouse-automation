<template>
  <div class="p-8 max-w-7xl">

    <!-- ── Page header ─────────────────────────────────────────── -->
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-[#1A1A1A]">{{ $t('reading.title') }}</h1>
      <p class="text-sm text-[#555555] mt-0.5">{{ $t('reading.value') }} · {{ $t('reading.unit') }}</p>
    </div>

    <!-- ── Sensor selector + New button ───────────────────────── -->
    <div class="flex items-end gap-4 mb-6">
      <div class="flex-1 max-w-xs">
        <label class="form-label">{{ $t('reading.selectSensor') }}</label>
        <select v-model="selectedSensorId" @change="loadReadings" class="form-input">
          <option :value="null">— {{ $t('reading.selectSensor') }} —</option>
          <option v-for="s in sensors" :key="s.id" :value="s.id">
            {{ s.name }} ({{ s.zoneName }})
          </option>
        </select>
      </div>
      <button
        class="btn-primary"
        @click="openCreate"
        :disabled="!selectedSensorId"
        :class="{ 'opacity-50 cursor-not-allowed': !selectedSensorId }"
      >
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
        </svg>
        {{ $t('reading.new') }}
      </button>
    </div>

    <!-- ── Loading ─────────────────────────────────────────────── -->
    <div v-if="loading" class="flex items-center justify-center py-24">
      <div class="w-10 h-10 border-4 border-greenhouse-200 border-t-greenhouse-600 rounded-full animate-spin"></div>
    </div>

    <!-- ── No sensor selected ──────────────────────────────────── -->
    <div v-else-if="!selectedSensorId"
         class="bg-white rounded-xl shadow-card border border-gray-100 py-16 text-center">
      <svg class="w-14 h-14 mx-auto text-greenhouse-200 mb-3" fill="currentColor" viewBox="0 0 24 24">
        <path d="M22 12h-4l-3 9L9 3l-3 9H2"/>
      </svg>
      <p class="text-sm text-gray-400 font-medium">{{ $t('reading.selectSensor') }}</p>
    </div>

    <!-- ── Table ───────────────────────────────────────────────── -->
    <div v-else class="bg-white rounded-xl shadow-card border border-gray-100 overflow-hidden">
      <table class="data-table">
        <thead>
          <tr>
            <th>{{ $t('reading.value') }}</th>
            <th>{{ $t('reading.unit') }}</th>
            <th>{{ $t('reading.recordedAt') }}</th>
            <th>{{ $t('common.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in readings" :key="r.id">
            <td>
              <span class="text-xl font-bold text-greenhouse-800 tabular-nums">{{ r.value }}</span>
            </td>
            <td>
              <span v-if="r.unit" class="font-mono text-xs bg-greenhouse-50 text-greenhouse-800 border border-greenhouse-200 rounded px-2 py-0.5">
                {{ r.unit }}
              </span>
              <span v-else class="text-gray-400">—</span>
            </td>
            <td class="text-[#555555] text-sm">{{ formatDate(r.recordedAt) }}</td>
            <td>
              <div class="flex items-center justify-end">
                <button class="btn-danger text-xs py-1.5 px-3" @click="confirmDelete(r)">
                  {{ $t('common.delete') }}
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="!readings.length">
            <td colspan="4" class="!py-16 text-center">
              <svg class="w-14 h-14 mx-auto text-greenhouse-200 mb-3" fill="currentColor" viewBox="0 0 24 24">
                <path d="M17 8C8 10 5.9 16.17 3.82 21.34L5.71 22l1-2.3A4.49 4.49 0 008 20C19 20 22 3 22 3c-1 2-8 2-8 2S5 5 5 11c0 3 2.5 5.5 5.5 5.5 2.5 0 4.5-2 4.5-2V8z"/>
              </svg>
              <p class="text-sm text-gray-400 font-medium">{{ $t('common.noData') }}</p>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Pagination -->
      <div
        v-if="totalPages > 1"
        class="flex items-center justify-between px-6 py-3.5 border-t border-gray-100 bg-greenhouse-50/30"
      >
        <span class="text-xs text-[#555555]">
          {{ currentPage * PAGE_SIZE + 1 }}–{{ Math.min((currentPage + 1) * PAGE_SIZE, totalElements) }}
          / {{ totalElements }}
        </span>
        <div class="flex items-center gap-1">
          <button
            class="btn-secondary text-xs py-1 px-2.5"
            :disabled="currentPage === 0"
            :class="{ 'opacity-40 cursor-not-allowed': currentPage === 0 }"
            @click="goToPage(currentPage - 1)"
          >‹ Prev</button>
          <span class="text-xs font-medium text-[#1A1A1A] px-3 py-1 rounded bg-white border border-gray-200">
            {{ currentPage + 1 }} / {{ totalPages }}
          </span>
          <button
            class="btn-secondary text-xs py-1 px-2.5"
            :disabled="currentPage >= totalPages - 1"
            :class="{ 'opacity-40 cursor-not-allowed': currentPage >= totalPages - 1 }"
            @click="goToPage(currentPage + 1)"
          >Next ›</button>
        </div>
      </div>
    </div>

    <!-- ── Create modal ────────────────────────────────────────── -->
    <Teleport to="body">
      <div v-if="showModal" class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
        <div class="bg-white rounded-2xl shadow-modal w-full max-w-md animate-fade-in">
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <h2 class="text-base font-semibold text-[#1A1A1A]">{{ $t('reading.new') }}</h2>
            <button class="btn-icon" @click="showModal = false">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>
          <form @submit.prevent="save" class="p-6 space-y-4">
            <div>
              <label class="form-label">{{ $t('reading.value') }} *</label>
              <input v-model.number="form.value" type="number" step="0.01" class="form-input" required />
            </div>
            <div>
              <label class="form-label">{{ $t('reading.unit') }}</label>
              <input v-model="form.unit" class="form-input bg-greenhouse-50 cursor-not-allowed" readonly />
            </div>
            <div class="flex justify-end gap-3 pt-2">
              <button type="button" class="btn-secondary" @click="showModal = false">{{ $t('common.cancel') }}</button>
              <button type="submit" class="btn-primary">{{ $t('common.save') }}</button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>

    <!-- ── Delete confirmation modal ──────────────────────────── -->
    <Teleport to="body">
      <div v-if="deleteTarget" class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
        <div class="bg-white rounded-2xl shadow-modal w-full max-w-sm p-6 animate-fade-in">
          <div class="flex items-start gap-4 mb-6">
            <div class="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center flex-shrink-0">
              <svg class="w-5 h-5 text-[#C62828]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
              </svg>
            </div>
            <div>
              <h3 class="font-semibold text-[#1A1A1A]">{{ $t('common.confirm') }}</h3>
              <p class="text-sm text-[#555555] mt-1">{{ $t('reading.deleteConfirm') }}</p>
            </div>
          </div>
          <div class="flex justify-end gap-3">
            <button class="btn-secondary" @click="deleteTarget = null">{{ $t('common.cancel') }}</button>
            <button class="btn-danger" @click="executeDelete">{{ $t('common.delete') }}</button>
          </div>
        </div>
      </div>
    </Teleport>

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
const deleteTarget = ref(null)
const form = ref({ value: 0, unit: '' })

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
  readings.value      = result.content       || []
  totalPages.value    = result.totalPages    ?? 0
  totalElements.value = result.totalElements ?? 0
  currentPage.value   = result.number        ?? 0
  loading.value = false
}

async function goToPage(page) { await fetchPage(page) }

const UNIT_MAP = {
  TEMPERATURA: '°C',
  HUMEDAD_AMBIENTAL: '%',
  HUMEDAD_SUELO: '%',
  LUZ: 'lux',
  PH: 'pH'
}

function openCreate() {
  const sensor = sensors.value.find(s => s.id === selectedSensorId.value)
  const unit = sensor?.type ? (UNIT_MAP[sensor.type] || '') : ''
  form.value = { value: 0, unit }
  showModal.value = true
}

async function save() {
  await sensorReadingService.create({ ...form.value, sensorId: selectedSensorId.value })
  showModal.value = false
  await fetchPage(currentPage.value)
}

function confirmDelete(item) { deleteTarget.value = item }

async function executeDelete() {
  if (!deleteTarget.value) return
  await sensorReadingService.remove(deleteTarget.value.id)
  readings.value = readings.value.filter(r => r.id !== deleteTarget.value.id)
  deleteTarget.value = null
}

function formatDate(dt) {
  if (!dt) return '—'
  return new Date(dt).toLocaleString()
}
</script>
