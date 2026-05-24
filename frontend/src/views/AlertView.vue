<template>
  <div class="p-8 max-w-7xl">

    <!-- ── Page header ─────────────────────────────────────────── -->
    <div class="flex items-center justify-between mb-8">
      <div>
        <h1 class="text-2xl font-bold text-[#1A1A1A]">{{ $t('alert.title') }}</h1>
        <p class="text-sm text-[#555555] mt-0.5">{{ $t('alert.severity') }} · {{ $t('common.status') }}</p>
      </div>
      <!-- Pending-only toggle -->
      <label
        class="flex items-center gap-2.5 cursor-pointer select-none
               bg-white border border-gray-200 rounded-xl px-4 py-2.5 shadow-sm
               hover:border-greenhouse-300 transition-colors"
      >
        <span class="relative inline-flex items-center">
          <input type="checkbox" v-model="pendingOnly" @change="load" class="sr-only peer" />
          <div class="w-9 h-5 bg-gray-200 rounded-full peer
                      peer-checked:bg-greenhouse-600 transition-colors duration-200"></div>
          <div class="absolute left-0.5 top-0.5 w-4 h-4 bg-white rounded-full shadow
                      transition-transform duration-200 peer-checked:translate-x-4"></div>
        </span>
        <span class="text-sm font-medium text-[#555555]">{{ $t('alert.pendingOnly') }}</span>
      </label>
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
            <th>{{ $t('alert.severity') }}</th>
            <th>{{ $t('alert.message') }}</th>
            <th>{{ $t('alert.sensor') }}</th>
            <th>{{ $t('alert.triggerValue') }}</th>
            <th>{{ $t('common.status') }}</th>
            <th>{{ $t('common.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="a in alerts"
            :key="a.id"
            :class="a.severity === 'CRITICA' && a.status === 'PENDIENTE' ? 'bg-red-50/40' : ''"
          >
            <td>
              <span :class="`badge-${a.severity.toLowerCase()}`">
                {{ $t(`alert.severities.${a.severity}`) }}
              </span>
            </td>
            <td :class="['max-w-xs', {
              'text-red-700 font-semibold': a.severity === 'CRITICA',
              'text-orange-600 font-medium': a.severity === 'ALTA',
              'text-yellow-700': a.severity === 'MEDIA',
              'text-[#1A1A1A]': a.severity === 'BAJA'
            }]">
              <p class="truncate" :title="a.message">{{ a.message }}</p>
            </td>
            <td class="text-[#555555]">{{ a.sensorName }}</td>
            <td>
              <span v-if="a.triggerValue != null"
                    class="font-mono text-xs bg-gray-50 border border-gray-200 rounded px-2 py-0.5">
                {{ a.triggerValue }}
              </span>
              <span v-else class="text-gray-400">—</span>
            </td>
            <td>
              <span :class="`badge-${a.status.toLowerCase()}`">
                {{ $t(`alert.statuses.${a.status}`) }}
              </span>
            </td>
            <td>
              <div class="flex items-center justify-end">
                <button
                  v-if="a.status === 'PENDIENTE'"
                  class="btn-secondary text-xs py-1.5 px-3
                         border-greenhouse-300 text-greenhouse-800 hover:bg-greenhouse-50"
                  @click="attend(a)"
                >
                  <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"/>
                  </svg>
                  {{ $t('alert.markAttended') }}
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="!alerts.length">
            <td colspan="6" class="!py-16 text-center">
              <svg class="w-14 h-14 mx-auto text-greenhouse-200 mb-3" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/>
              </svg>
              <p class="text-sm text-gray-400 font-medium">{{ $t('common.noData') }}</p>
            </td>
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
