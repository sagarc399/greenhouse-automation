<template>
  <div class="p-8 max-w-7xl">

    <!-- ── Page header ─────────────────────────────────────────── -->
    <div class="flex items-center justify-between mb-8">
      <div>
        <h1 class="text-2xl font-bold text-[#1A1A1A]">{{ $t('rule.title') }}</h1>
        <p class="text-sm text-[#555555] mt-0.5">{{ $t('rule.sensor') }} · {{ $t('rule.condition') }}</p>
      </div>
      <button class="btn-primary" @click="openCreate">
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/>
        </svg>
        {{ $t('rule.new') }}
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
            <th>{{ $t('rule.sensor') }}</th>
            <th>{{ $t('rule.condition') }}</th>
            <th>{{ $t('rule.actuator') }}</th>
            <th>{{ $t('rule.active') }}</th>
            <th>{{ $t('common.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in rules" :key="r.id">
            <td>
              <div class="flex items-center gap-3">
                <div class="w-8 h-8 bg-indigo-50 rounded-lg flex items-center justify-center text-base flex-shrink-0">🤖</div>
                <span class="font-semibold text-[#1A1A1A]">{{ r.name }}</span>
              </div>
            </td>
            <td class="text-[#555555]">{{ r.sensorName }}</td>
            <td>
              <span class="text-xs font-mono bg-gray-50 border border-gray-200 rounded-md px-2 py-1 text-[#1A1A1A]">
                {{ $t(`rule.operators.${r.operator}`) }} {{ r.threshold }}
              </span>
            </td>
            <td class="text-[#555555]">{{ r.actuatorName || '—' }}</td>
            <td>
              <span :class="r.active ? 'badge-active' : 'badge-inactive'">
                {{ r.active ? $t('rule.active') : '—' }}
              </span>
            </td>
            <td>
              <div class="flex items-center justify-end gap-2">
                <button class="btn-secondary text-xs py-1.5 px-3" @click="openEdit(r)">{{ $t('common.edit') }}</button>
                <button class="btn-danger    text-xs py-1.5 px-3" @click="confirmDelete(r)">{{ $t('common.delete') }}</button>
              </div>
            </td>
          </tr>
          <tr v-if="!rules.length">
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
        <div class="bg-white rounded-2xl shadow-modal w-full max-w-lg animate-fade-in">
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
            <h2 class="text-base font-semibold text-[#1A1A1A]">
              {{ editingItem ? $t('rule.edit') : $t('rule.new') }}
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
                <label class="form-label">{{ $t('common.zone') }} *</label>
                <select v-model="form.zoneId" class="form-input" required>
                  <option v-for="z in zones" :key="z.id" :value="z.id">{{ z.name }}</option>
                </select>
              </div>
              <div>
                <label class="form-label">{{ $t('rule.sensor') }} *</label>
                <select v-model="form.sensorId" class="form-input" required>
                  <option v-for="s in sensors" :key="s.id" :value="s.id">{{ s.name }}</option>
                </select>
              </div>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="form-label">{{ $t('rule.operator') }} *</label>
                <select v-model="form.operator" class="form-input" required>
                  <option v-for="op in operators" :key="op" :value="op">{{ $t(`rule.operators.${op}`) }}</option>
                </select>
              </div>
              <div>
                <label class="form-label">{{ $t('rule.threshold') }} *</label>
                <input v-model.number="form.threshold" type="number" step="0.1" class="form-input" required />
              </div>
            </div>
            <!-- Divider -->
            <div class="border-t border-gray-100 pt-2">
              <p class="text-xs text-[#555555] font-semibold uppercase tracking-wide mb-3">
                {{ $t('rule.targetActuator') }}
              </p>
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="form-label">{{ $t('rule.actuator') }}</label>
                  <select v-model="form.actuatorId" class="form-input">
                    <option :value="null">{{ $t('common.none') }}</option>
                    <option v-for="a in actuators" :key="a.id" :value="a.id">{{ a.name }}</option>
                  </select>
                </div>
                <div>
                  <label class="form-label">{{ $t('rule.targetState') }}</label>
                  <select v-model="form.actuatorTargetState" class="form-input">
                    <option :value="null">—</option>
                    <option value="ENCENDIDO">{{ $t('actuator.states.ENCENDIDO') }}</option>
                    <option value="APAGADO">{{ $t('actuator.states.APAGADO') }}</option>
                  </select>
                </div>
              </div>
            </div>
            <div class="flex items-center gap-3 p-3 bg-greenhouse-50 rounded-lg">
              <input type="checkbox" v-model="form.active" id="ruleActive"
                     class="w-4 h-4 rounded border-gray-300 text-greenhouse-700 focus:ring-greenhouse-500" />
              <label for="ruleActive" class="text-sm font-medium text-[#1A1A1A] cursor-pointer">
                {{ $t('rule.active') }}
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
              <p class="text-sm text-[#555555] mt-1">{{ $t('rule.deleteConfirm') }}</p>
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
import { automationRuleService } from '@/services/automationRuleService'
import { zoneService } from '@/services/zoneService'
import { sensorService } from '@/services/sensorService'
import { actuatorService } from '@/services/actuatorService'

const { t } = useI18n()
const rules = ref([])
const zones = ref([])
const sensors = ref([])
const actuators = ref([])
const loading = ref(true)
const showModal = ref(false)
const editingItem = ref(null)
const deleteTarget = ref(null)
const operators = ['MAYOR_QUE', 'MENOR_QUE', 'IGUAL_QUE']

const defaultForm = () => ({
  name: '', zoneId: null, sensorId: null, operator: 'MAYOR_QUE',
  threshold: 0, actuatorId: null, actuatorTargetState: null, active: true
})
const form = ref(defaultForm())

onMounted(async () => {
  ;[rules.value, zones.value, sensors.value, actuators.value] = await Promise.all([
    automationRuleService.getAll(), zoneService.getAll(),
    sensorService.getAll(), actuatorService.getAll()
  ])
  loading.value = false
})

function openCreate() {
  editingItem.value = null
  form.value = defaultForm()
  showModal.value = true
}

function openEdit(item) {
  editingItem.value = item
  form.value = { ...item }
  showModal.value = true
}

async function save() {
  if (editingItem.value) {
    const updated = await automationRuleService.update(editingItem.value.id, form.value)
    const idx = rules.value.findIndex(r => r.id === editingItem.value.id)
    if (idx !== -1) rules.value[idx] = updated
  } else {
    rules.value.push(await automationRuleService.create(form.value))
  }
  showModal.value = false
}

function confirmDelete(item) { deleteTarget.value = item }

async function executeDelete() {
  if (!deleteTarget.value) return
  await automationRuleService.remove(deleteTarget.value.id)
  rules.value = rules.value.filter(r => r.id !== deleteTarget.value.id)
  deleteTarget.value = null
}
</script>
