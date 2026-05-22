<template>
  <div class="p-8">
    <h1 class="text-2xl font-bold text-gray-900 mb-8">{{ $t('dashboard.title') }}</h1>

    <div v-if="loading" class="text-gray-500">{{ $t('common.loading') }}</div>

    <div v-else class="grid grid-cols-2 lg:grid-cols-4 gap-6">
      <DashboardCard
        :label="$t('dashboard.greenhouses')"
        :value="stats.totalGreenhouses"
        icon="🏠"
        color="bg-green-50 text-green-700"
      />
      <DashboardCard
        :label="$t('dashboard.zones')"
        :value="stats.totalZones"
        icon="🗂️"
        color="bg-blue-50 text-blue-700"
      />
      <DashboardCard
        :label="$t('dashboard.activeSensors')"
        :value="stats.activeSensors"
        icon="📡"
        color="bg-purple-50 text-purple-700"
      />
      <DashboardCard
        :label="$t('dashboard.runningActuators')"
        :value="stats.activeActuators"
        icon="⚙️"
        color="bg-orange-50 text-orange-700"
      />
      <DashboardCard
        :label="$t('dashboard.activeRules')"
        :value="stats.activeRules"
        icon="🤖"
        color="bg-indigo-50 text-indigo-700"
      />
      <DashboardCard
        :label="$t('dashboard.pendingAlerts')"
        :value="stats.pendingAlerts"
        icon="🔔"
        color="bg-yellow-50 text-yellow-700"
      />
      <DashboardCard
        :label="$t('dashboard.criticalAlerts')"
        :value="stats.criticalAlerts"
        icon="🚨"
        color="bg-red-50 text-red-700"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import DashboardCard from '@/components/DashboardCard.vue'
import { dashboardService } from '@/services/dashboardService'

const loading = ref(true)
const stats = ref({
  totalGreenhouses: 0,
  totalZones: 0,
  activeSensors: 0,
  activeActuators: 0,
  activeRules: 0,
  pendingAlerts: 0,
  criticalAlerts: 0
})

onMounted(async () => {
  try {
    stats.value = await dashboardService.getDashboard()
  } finally {
    loading.value = false
  }
})
</script>
