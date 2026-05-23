<template>
  <div class="min-h-screen flex bg-gray-50">
    <!-- Sidebar -->
    <aside class="w-64 bg-greenhouse-800 text-white flex flex-col">
      <div class="p-6 border-b border-greenhouse-700">
        <h1 class="text-xl font-bold">🌱 Greenhouse</h1>
        <p class="text-greenhouse-300 text-xs mt-1">Management System</p>
      </div>

      <nav class="flex-1 p-4 space-y-1">
        <RouterLink
          v-for="link in visibleLinks"
          :key="link.to"
          :to="link.to"
          class="flex items-center gap-3 px-4 py-2.5 rounded-lg text-greenhouse-100 hover:bg-greenhouse-700 transition-colors"
          active-class="bg-greenhouse-700 text-white font-medium"
        >
          <span class="text-lg">{{ link.icon }}</span>
          <span class="text-sm">{{ $t(link.label) }}</span>
        </RouterLink>
      </nav>

      <div class="p-4 border-t border-greenhouse-700">
        <div class="flex items-center gap-3 mb-3">
          <div class="w-8 h-8 rounded-full bg-greenhouse-600 flex items-center justify-center text-sm font-bold">
            {{ userInitial }}
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium truncate">{{ authStore.user?.name }}</p>
            <p class="text-xs text-greenhouse-300">{{ authStore.user?.role }}</p>
          </div>
        </div>
        <div class="flex items-center justify-between">
          <button
            @click="toggleLocale"
            class="text-xs text-greenhouse-300 hover:text-white transition-colors"
          >
            {{ currentLocale === 'es' ? 'EN' : 'ES' }}
          </button>
          <button
            @click="authStore.logout()"
            class="text-xs text-greenhouse-300 hover:text-white transition-colors"
          >
            {{ $t('nav.logout') }}
          </button>
        </div>
      </div>
    </aside>

    <!-- Main content -->
    <main class="flex-1 overflow-auto">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const { locale } = useI18n()

const currentLocale = computed(() => locale.value)

function toggleLocale() {
  const next = locale.value === 'es' ? 'en' : 'es'
  locale.value = next
  localStorage.setItem('locale', next)
}

const userInitial = computed(() =>
  authStore.user?.name?.charAt(0).toUpperCase() || '?'
)

const allLinks = [
  { to: '/dashboard',        icon: '📊', label: 'nav.dashboard', roles: ['ADMIN', 'OPERATOR'] },
  { to: '/greenhouses',      icon: '🏠', label: 'nav.greenhouses', roles: ['ADMIN'] },
  { to: '/zones',            icon: '🗂️', label: 'nav.zones', roles: ['ADMIN'] },
  { to: '/sensors',          icon: '📡', label: 'nav.sensors', roles: ['ADMIN'] },
  { to: '/actuators',        icon: '⚙️', label: 'nav.actuators', roles: ['ADMIN'] },
  { to: '/automation-rules', icon: '🤖', label: 'nav.rules', roles: ['ADMIN'] },
  { to: '/alerts',           icon: '🔔', label: 'nav.alerts', roles: ['ADMIN', 'OPERATOR'] },
  { to: '/sensor-readings',  icon: '📈', label: 'nav.readings', roles: ['ADMIN', 'OPERATOR'] }
]

const visibleLinks = computed(() =>
  allLinks.filter(link => link.roles.includes(authStore.user?.role))
)
</script>
