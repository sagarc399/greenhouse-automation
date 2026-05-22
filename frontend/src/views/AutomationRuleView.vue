<template>
  <div class="p-8">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-900">{{ $t('rule.title') }}</h1>
      <button class="btn-primary" @click="openCreate">+ {{ $t('rule.new') }}</button>
    </div>

    <div v-if="loading" class="text-gray-500">{{ $t('common.loading') }}</div>

    <div v-else class="card overflow-hidden p-0">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b border-gray-200">
          <tr>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('common.name') }}</th>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('rule.sensor') }}</th>
            <th class="text-left px-6 py-3 font-medium text-gray-600">Condition</th>
            <th class="text-left px-6 py-3 font-medium text-gray-600">Actuator</th>
            <th class="text-left px-6 py-3 font-medium text-gray-600">Active</th>
            <th class="text-right px-6 py-3 font-medium text-gray-600">{{ $t('common.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in rules" :key="r.id" class="border-b border-gray-100 hover:bg-gray-50">
            <td class="px-6 py-4 font-medium">{{ r.name }}</td>
            <td class="px-6 py-4 text-gray-500">{{ r.sensorName }}</td>
            <td class="px-6 py-4">{{ $t(`rule.operators.${r.operator}`) }} {{ r.threshold }}</td>
            <td class="px-6 py-4 text-gray-500">{{ r.actuatorName || '—' }}</td>
            <td class="px-6 py-4">
              <span :class="r.active ? 'text-green-600' : 'text-red-500'">{{ r.active ? '✓' : '✗' }}</span>
            </td>
            <td class="px-6 py-4 text-right space-x-2">
              <button class="btn-secondary text-xs" @click="openEdit(r)">{{ $t('common.edit') }}</button>
              <button class="btn-danger text-xs" @click="remove(r)">{{ $t('common.delete') }}</button>
            </td>
          </tr>
          <tr v-if="!rules.length">
            <td colspan="6" class="px-6 py-8 text-center text-gray-400">{{ $t('common.noData') }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div class="bg-white rounded-2xl shadow-2xl p-6 w-full max-w-lg">
        <h2 class="text-lg font-bold mb-4">{{ editingItem ? $t('rule.edit') : $t('rule.new') }}</h2>
        <form @submit.prevent="save">
          <div class="space-y-4">
            <div>
              <label class="form-label">{{ $t('common.name') }} *</label>
              <input v-model="form.name" class="form-input" required />
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="form-label">Zone *</label>
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
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="form-label">{{ $t('rule.targetActuator') }}</label>
                <select v-model="form.actuatorId" class="form-input">
                  <option :value="null">None</option>
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
            <div class="flex items-center gap-2">
              <input type="checkbox" v-model="form.active" id="ruleActive" />
              <label for="ruleActive" class="text-sm text-gray-700">Active</label>
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
import { automationRuleService } from '@/services/automationRuleService'
import { zoneService } from '@/services/zoneService'
import { sensorService } from '@/services/sensorService'
import { actuatorService } from '@/services/actuatorService'

const rules = ref([])
const zones = ref([])
const sensors = ref([])
const actuators = ref([])
const loading = ref(true)
const showModal = ref(false)
const editingItem = ref(null)
const operators = ['MAYOR_QUE', 'MENOR_QUE', 'IGUAL_QUE']

const defaultForm = () => ({
  name: '', zoneId: null, sensorId: null, operator: 'MAYOR_QUE',
  threshold: 0, actuatorId: null, actuatorTargetState: null, active: true
})
const form = ref(defaultForm())

onMounted(async () => {
  [rules.value, zones.value, sensors.value, actuators.value] = await Promise.all([
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

async function remove(item) {
  if (confirm(`Delete rule "${item.name}"?`)) {
    await automationRuleService.remove(item.id)
    rules.value = rules.value.filter(r => r.id !== item.id)
  }
}
</script>
