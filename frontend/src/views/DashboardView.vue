<template>
  <div class="p-8 max-w-7xl">

    <!-- ── Welcome header ─────────────────────────────────────── -->
    <div class="mb-8">
      <div class="flex items-start justify-between">
        <div>
          <h1 class="text-2xl font-bold text-[#1A1A1A]">
            {{ $t('auth.welcome') }}, {{ authStore.user?.name?.split(' ')[0] }} 👋
          </h1>
          <p class="text-[#555555] text-sm mt-1">{{ today }}</p>
        </div>
        <div class="hidden sm:flex items-center gap-2 bg-white rounded-xl px-4 py-2.5 shadow-card border border-gray-100">
          <span class="text-lg">🌿</span>
          <span class="text-sm font-medium text-greenhouse-800">{{ $t('dashboard.title') }}</span>
        </div>
      </div>
    </div>

    <!-- ── Loading ─────────────────────────────────────────────── -->
    <div v-if="loading" class="flex items-center justify-center py-24">
      <div class="w-10 h-10 border-4 border-greenhouse-200 border-t-greenhouse-600 rounded-full animate-spin"></div>
    </div>

    <template v-else>

      <!-- ── Stat cards grid ─────────────────────────────────── -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5 mb-8">
        <DashboardCard
          :label="$t('dashboard.greenhouses')"
          :value="stats.totalGreenhouses"
          icon="🏠"
          border-color="#2E7D32"
          icon-bg="#F1F8E9"
        />
        <DashboardCard
          :label="$t('dashboard.zones')"
          :value="stats.totalZones"
          icon="🗂️"
          border-color="#0277BD"
          icon-bg="#E1F5FE"
        />
        <DashboardCard
          :label="$t('dashboard.activeSensors')"
          :value="stats.activeSensors"
          icon="📡"
          border-color="#6A1B9A"
          icon-bg="#F3E5F5"
        />
        <DashboardCard
          :label="$t('dashboard.runningActuators')"
          :value="stats.activeActuators"
          icon="⚙️"
          border-color="#BF360C"
          icon-bg="#FBE9E7"
        />
        <DashboardCard
          :label="$t('dashboard.activeRules')"
          :value="stats.activeRules"
          icon="🤖"
          border-color="#1565C0"
          icon-bg="#E3F2FD"
        />
        <DashboardCard
          :label="$t('dashboard.pendingAlerts')"
          :value="stats.pendingAlerts"
          icon="🔔"
          border-color="#F57F17"
          icon-bg="#FFF9C4"
        />
        <DashboardCard
          :label="$t('dashboard.criticalAlerts')"
          :value="stats.criticalAlerts"
          icon="🚨"
          border-color="#C62828"
          icon-bg="#FFEBEE"
        />
      </div>

      <!-- ── Recent alerts ───────────────────────────────────── -->
      <div class="bg-white rounded-xl shadow-card border border-gray-100 overflow-hidden">
        <!-- Header -->
        <div class="flex items-center justify-between px-6 py-4 border-b border-gray-100">
          <div class="flex items-center gap-2">
            <span class="text-lg">🔔</span>
            <h2 class="font-semibold text-[#1A1A1A] text-sm">{{ $t('alert.title') }}</h2>
            <span
              v-if="recentAlerts.length"
              class="inline-flex items-center justify-center w-5 h-5 text-xs font-bold
                     bg-amber-100 text-amber-700 rounded-full"
            >{{ recentAlerts.length }}</span>
          </div>
          <RouterLink
            to="/alerts"
            class="text-xs text-greenhouse-700 hover:text-greenhouse-900 font-medium
                   hover:underline transition-colors"
          >
            View all →
          </RouterLink>
        </div>

        <!-- Alert rows -->
        <div v-if="loadingAlerts" class="flex items-center justify-center py-10">
          <div class="w-6 h-6 border-3 border-greenhouse-200 border-t-greenhouse-500 rounded-full animate-spin"></div>
        </div>

        <div v-else-if="recentAlerts.length === 0" class="py-12 text-center">
          <svg class="w-12 h-12 mx-auto text-greenhouse-200 mb-3" fill="currentColor" viewBox="0 0 24 24">
            <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/>
          </svg>
          <p class="text-sm text-gray-400 font-medium">{{ $t('common.noData') }}</p>
        </div>

        <table v-else class="w-full text-sm">
          <thead>
            <tr class="bg-greenhouse-50 border-b border-greenhouse-100">
              <th class="px-6 py-3 text-left text-xs font-semibold text-greenhouse-900 uppercase tracking-wide">
                {{ $t('alert.severity') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-greenhouse-900 uppercase tracking-wide">
                {{ $t('alert.message') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-greenhouse-900 uppercase tracking-wide">
                {{ $t('alert.sensor') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-greenhouse-900 uppercase tracking-wide">
                {{ $t('common.status') }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="a in recentAlerts"
              :key="a.id"
              class="border-t border-gray-100 hover:bg-greenhouse-50/60 transition-colors"
            >
              <td class="px-6 py-3.5">
                <span :class="`badge-${a.severity.toLowerCase()}`">
                  {{ $t(`alert.severities.${a.severity}`) }}
                </span>
              </td>
              <td class="px-6 py-3.5 text-[#1A1A1A] max-w-xs truncate">{{ a.message }}</td>
              <td class="px-6 py-3.5 text-[#555555]">{{ a.sensorName }}</td>
              <td class="px-6 py-3.5">
                <span :class="`badge-${a.status.toLowerCase()}`">
                  {{ $t(`alert.statuses.${a.status}`) }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import DashboardCard from '@/components/DashboardCard.vue'
import { dashboardService } from '@/services/dashboardService'
import { alertService } from '@/services/alertService'
import { useAuthStore } from '@/stores/auth'

const { locale } = useI18n()
const authStore = useAuthStore()

const loading = ref(true)
const loadingAlerts = ref(true)

const stats = ref({
  totalGreenhouses: 0,
  totalZones: 0,
  activeSensors: 0,
  activeActuators: 0,
  activeRules: 0,
  pendingAlerts: 0,
  criticalAlerts: 0
})
const recentAlerts = ref([])

const today = computed(() =>
  new Date().toLocaleDateString(locale.value === 'es' ? 'es-ES' : 'en-US', {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
  })
)

onMounted(async () => {
  try {
    [stats.value] = await Promise.all([dashboardService.getDashboard()])
  } finally {
    loading.value = false
  }
  try {
    const all = await alertService.getAll(true)
    recentAlerts.value = all.slice(0, 5)
  } finally {
    loadingAlerts.value = false
  }
})
</script>
