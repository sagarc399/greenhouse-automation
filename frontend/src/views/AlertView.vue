<template>
  <div class="p-8">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-900">{{ $t('alert.title') }}</h1>
      <label class="flex items-center gap-2 text-sm text-gray-600 cursor-pointer">
        <input type="checkbox" v-model="pendingOnly" @change="load" class="rounded" />
        {{ $t('alert.pendingOnly') }}
      </label>
    </div>

    <div v-if="loading" class="text-gray-500">{{ $t('common.loading') }}</div>

    <div v-else class="card overflow-hidden p-0">
      <table class="w-full text-sm">
        <thead class="bg-gray-50 border-b border-gray-200">
          <tr>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('alert.severity') }}</th>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('alert.message') }}</th>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('alert.sensor') }}</th>
            <th class="text-left px-6 py-3 font-medium text-gray-600">{{ $t('common.status') }}</th>
            <th class="text-right px-6 py-3 font-medium text-gray-600">{{ $t('common.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="a in alerts" :key="a.id" class="border-b border-gray-100 hover:bg-gray-50">
            <td class="px-6 py-4">
              <span :class="`badge-${a.severity.toLowerCase()}`">
                {{ $t(`alert.severities.${a.severity}`) }}
              </span>
            </td>
            <td class="px-6 py-4 max-w-sm truncate">{{ a.message }}</td>
            <td class="px-6 py-4 text-gray-500">{{ a.sensorName }}</td>
            <td class="px-6 py-4">
              <span :class="`badge-${a.status.toLowerCase()}`">
                {{ $t(`alert.statuses.${a.status}`) }}
              </span>
            </td>
            <td class="px-6 py-4 text-right">
              <button
                v-if="a.status === 'PENDIENTE'"
                class="btn-secondary text-xs"
                @click="attend(a)"
              >
                {{ $t('alert.markAttended') }}
              </button>
            </td>
          </tr>
          <tr v-if="!alerts.length">
            <td colspan="5" class="px-6 py-8 text-center text-gray-400">{{ $t('common.noData') }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { alertService } from '@/services/alertService'

const alerts = ref([])
const loading = ref(true)
const pendingOnly = ref(false)

async function load() {
  loading.value = true
  alerts.value = await alertService.getAll(pendingOnly.value)
  loading.value = false
}

onMounted(load)

async function attend(item) {
  const updated = await alertService.updateStatus(item.id, 'ATENDIDA')
  const idx = alerts.value.findIndex(a => a.id === item.id)
  if (idx !== -1) alerts.value[idx] = updated
}
</script>
