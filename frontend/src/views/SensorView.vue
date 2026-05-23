<template>
  <div class="p-8 max-w-7xl">

    <!-- ── Page header ─────────────────────────────────────────── -->
    <div class="flex items-center justify-between mb-8">
      <div>
        <h1 class="text-2xl font-bold text-[#1A1A1A]">{{ $t('sensor.title') }}</h1>
        <p class="text-sm text-[#555555] mt-0.5">{{ $t('common.type') }} · {{ $t('common.zone') }}</p>
      </div>
      <button class="btn-primary" @click="openCreate">
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
        </svg>
        {{ $t('sensor.new') }}
      </button>
    </div>

    <!-- ── Loading ─────────────────────────────────────────────── -->
    <div v-if="loading" class="flex items-center justify-center py-24">
      <div class="w-10 h-10 border-4 border-greenhouse-200 border-t-greenhouse-600 rounded-full animate-spin"></div>
    </div>

    <!-- ── Table ───────────────────────────────────────────────── -->
    <div v-else class="bg-white rounded-xl shadow-card border border-gray-100 overflow-hidden">
      <table class="data-table">
        <thead>
          <tr>
            <th>{{ $t('common.name') }}</th>
            <th>{{ $t('common.type') }}</th>
            <th>{{ $t('common.zone') }}</th>
            <th>{{ $t('sensor.model') }}</th>
            <th>{{ $t('common.active') }}</th>
            <th>{{ $t('common.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="s in sensors" :key="s.id">
            <td>
              <div class="flex items-center gap-3">
                <div class="w-8 h-8 bg-purple-50 rounded-lg flex items-center justify-center text-base flex-shrink-0">📡</div>
                <span class="font-semibold text-[#1A1A1A]">{{ s.name }}</span>
              </div>
            </td>
            <td>
              <span class="badge bg-gray-100 text-gray-700">{{ $t(`sensor.types.${s.type}`) }}</span>
            </td>
            <td class="text-[#555555]">{{ s.zoneName }}</td>
            <td class="text-[#555555] text-xs font-mono">{{ s.model || '—' }}</td>
            <td>
              <span :class="s.active ? 'badge-active' : 'badge-inactive'">
                {{ s.active ? $t('common.active') : '—' }}
              </span>
            </td>
            <td>
              <div class="flex items-center justify-end gap-2">
                <button class="btn-secondary text-xs py-1.5 px-3" @click="openEdit(s)">{{ $t('common.edit') }}</button>
                <button class="btn-danger    text-xs py-1.5 px-3" @click="confirmDelete(s)">{{ $t('common.delete') }}</button>
              </div>
            </td>
          </tr>
          <tr v-if="!sensors.length">
            <td colspan="6" class="!py-16 text-center">
              <svg class="w-14 h-14 mx-auto text-greenhouse-200 mb-3" fill="currentColor" viewBox="0 0 24 24">
                <path d="M17 8C8 10 5.9 16.17 3.82 21.34L5.71 22l1-2.3A4.49 4.49 0 008 20C19 20 22 3 22 3c-1 2-8 2-8 2S5 5 5 11c0 3 2.5 5.5 5.5 5.5 2.5 0 4.5-2 4.5-2V8z"/>
              </svg>
              <p class="text-sm text-gray-400 font-medium">{{ $t('common.noData') }}</p>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- ── Create / Edit modal ─────────────────────────────────── -->
    <Teleport to="body">
      <div v-if="showModal" class="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center z-50 p-4">
        <div class="bg-white rounded-2xl shadow-modal w-full max-w-md animate-fade-in">
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <h2 class="text-base font-semibold text-[#1A1A1A]">
              {{ editingItem ? $t('sensor.edit') : $t('sensor.new') }}
            </h2>
            <button class="btn-icon" @click="showModal = false">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
              </svg>
            </button>
          </div>
          <form @submit.prevent="save" class="p-6 space-y-4">
            <div>
              <label class="form-label">{{ $t('common.name') }} *</label>
              <input v-model="form.name" class="form-input" required />
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="form-label">{{ $t('common.type') }} *</label>
                <select v-model="form.type" class="form-input" required>
                  <option v-for="stype in sensorTypes" :key="stype" :value="stype">
                    {{ $t(`sensor.types.${stype}`) }}
                  </option>
                </select>
              </div>
              <div>
                <label class="form-label">{{ $t('common.zone') }} *</label>
                <select v-model="form.zoneId" class="form-input" required>
                  <option v-for="z in zones" :key="z.id" :value="z.id">{{ z.name }}</option>
                </select>
              </div>
            </div>
            <div>
              <label class="form-label">{{ $t('sensor.model') }}</label>
              <input v-model="form.model" class="form-input" />
            </div>
            <div class="flex items-center gap-3 p-3 bg-greenhouse-50 rounded-lg">
              <input type="checkbox" v-model="form.active" id="sensorActive"
                     class="w-4 h-4 rounded border-gray-300 text-greenhouse-700 focus:ring-greenhouse-500" />
              <label for="sensorActive" class="text-sm font-medium text-[#1A1A1A] cursor-pointer">
                {{ $t('common.active') }}
              </label>
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
              <p class="text-sm text-[#555555] mt-1">{{ $t('sensor.deleteConfirm') }}</p>
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
import { zoneService } from '@/services/zoneService'

const { t } = useI18n()
const sensors = ref([])
const zones = ref([])
const loading = ref(true)
const showModal = ref(false)
const editingItem = ref(null)
const deleteTarget = ref(null)
const form = ref({ name: '', type: 'TEMPERATURA', zoneId: null, model: '', active: true })

const sensorTypes = ['TEMPERATURA', 'HUMEDAD_AMBIENTAL', 'HUMEDAD_SUELO', 'LUZ', 'PH']

onMounted(async () => {
  ;[sensors.value, zones.value] = await Promise.all([sensorService.getAll(), zoneService.getAll()])
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

function confirmDelete(item) { deleteTarget.value = item }

async function executeDelete() {
  if (!deleteTarget.value) return
  await sensorService.remove(deleteTarget.value.id)
  sensors.value = sensors.value.filter(s => s.id !== deleteTarget.value.id)
  deleteTarget.value = null
}
</script>
