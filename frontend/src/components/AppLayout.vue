<template>
  <div class="min-h-screen bg-greenhouse-50">

    <!-- ── Sidebar ─────────────────────────────────────────────── -->
    <aside
      class="fixed inset-y-0 left-0 w-[280px] flex flex-col z-30
             bg-gradient-to-b from-greenhouse-900 via-greenhouse-900 to-greenhouse-800
             shadow-[4px_0_24px_rgba(0,0,0,0.15)]"
    >
      <!-- Logo -->
      <div class="px-6 py-5 border-b border-white/10">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-greenhouse-400 rounded-xl flex items-center justify-center text-xl shadow-sm flex-shrink-0">
            🌱
          </div>
          <div>
            <p class="text-white text-sm font-bold leading-tight tracking-wide">Greenhouse</p>
            <p class="text-greenhouse-300 text-[10px] uppercase tracking-widest mt-0.5">Management System</p>
          </div>
        </div>
      </div>

      <!-- Navigation -->
      <nav class="flex-1 py-5 px-3 space-y-0.5 overflow-y-auto">
        <RouterLink
          v-for="link in visibleLinks"
          :key="link.to"
          :to="link.to"
          class="flex items-center gap-3 px-3 py-2.5 rounded-lg
                 text-greenhouse-200 text-sm font-medium
                 hover:bg-white/10 hover:text-white
                 border-l-[3px] border-transparent
                 transition-all duration-150 group"
          active-class="!bg-white/15 !text-white !border-greenhouse-400"
        >
          <span class="text-base w-5 text-center leading-none">{{ link.icon }}</span>
          <span>{{ $t(link.label) }}</span>
        </RouterLink>
      </nav>

      <!-- User area -->
      <div class="p-4 border-t border-white/10">
        <!-- User card -->
        <div class="flex items-center gap-3 p-3 rounded-xl bg-white/8 mb-3">
          <div
            class="w-9 h-9 rounded-full bg-greenhouse-400 flex items-center justify-center
                   text-sm font-bold text-greenhouse-900 flex-shrink-0 shadow-sm"
          >
            {{ userInitial }}
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-white text-sm font-semibold truncate leading-tight">
              {{ authStore.user?.name }}
            </p>
            <p class="text-greenhouse-300 text-xs mt-0.5">{{ authStore.user?.role }}</p>
          </div>
        </div>

        <!-- Actions row -->
        <div class="flex items-center justify-between px-1">
          <button
            @click="toggleLocale"
            class="text-xs text-greenhouse-400 hover:text-white
                   transition-colors duration-150 font-medium px-2 py-1 rounded hover:bg-white/10"
          >
            {{ currentLocale === 'es' ? '🇺🇸 EN' : '🇪🇸 ES' }}
          </button>
          <button
            @click="authStore.logout()"
            class="flex items-center gap-1.5 text-xs text-greenhouse-400 hover:text-white
                   transition-colors duration-150 px-2 py-1 rounded hover:bg-white/10"
          >
            <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/>
            </svg>
            {{ $t('nav.logout') }}
          </button>
        </div>
      </div>
    </aside>

    <!-- ── Main content ────────────────────────────────────────── -->
    <div class="pl-[280px] min-h-screen">
      <main class="min-h-screen">
        <router-view />
      </main>
    </div>

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
  { to: '/dashboard',        icon: '📊', label: 'nav.dashboard',  roles: ['ADMIN', 'OPERATOR'] },
  { to: '/greenhouses',      icon: '🏠', label: 'nav.greenhouses', roles: ['ADMIN'] },
  { to: '/zones',            icon: '🗂️', label: 'nav.zones',       roles: ['ADMIN'] },
  { to: '/sensors',          icon: '📡', label: 'nav.sensors',     roles: ['ADMIN'] },
  { to: '/actuators',        icon: '⚙️', label: 'nav.actuators',   roles: ['ADMIN'] },
  { to: '/automation-rules', icon: '🤖', label: 'nav.rules',       roles: ['ADMIN'] },
  { to: '/alerts',           icon: '🔔', label: 'nav.alerts',      roles: ['ADMIN', 'OPERATOR'] },
  { to: '/sensor-readings',  icon: '📈', label: 'nav.readings',    roles: ['ADMIN', 'OPERATOR'] }
]

const visibleLinks = computed(() =>
  allLinks.filter(link => link.roles.includes(authStore.user?.role))
)
</script>
