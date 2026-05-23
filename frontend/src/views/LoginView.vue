<template>
  <div class="min-h-screen flex">

    <!-- ── Left panel — branding ──────────────────────────────── -->
    <div
      class="hidden lg:flex lg:w-1/2 flex-col justify-between p-12
             bg-gradient-to-br from-greenhouse-900 via-greenhouse-800 to-greenhouse-700
             relative overflow-hidden"
    >
      <!-- Background decoration -->
      <div class="absolute inset-0 opacity-10">
        <div class="absolute top-[-80px] left-[-80px] w-96 h-96 bg-greenhouse-400 rounded-full blur-3xl"></div>
        <div class="absolute bottom-[-60px] right-[-60px] w-80 h-80 bg-greenhouse-300 rounded-full blur-3xl"></div>
      </div>

      <!-- Logo -->
      <div class="relative flex items-center gap-3">
        <div class="w-11 h-11 bg-greenhouse-400 rounded-2xl flex items-center justify-center text-2xl shadow-lg">
          🌱
        </div>
        <div>
          <p class="text-white font-bold text-lg leading-tight">Greenhouse</p>
          <p class="text-greenhouse-300 text-xs uppercase tracking-widest">Management System</p>
        </div>
      </div>

      <!-- Center content -->
      <div class="relative">
        <div class="text-6xl mb-6">🌿</div>
        <h2 class="text-4xl font-bold text-white leading-tight mb-4">
          {{ $t('auth.loginDesc') }}
        </h2>
        <p class="text-greenhouse-200 text-lg leading-relaxed max-w-sm">
          Monitor your greenhouses, automate responses, and keep your crops thriving.
        </p>

        <!-- Feature dots -->
        <div class="mt-8 space-y-3">
          <div v-for="feat in features" :key="feat.text" class="flex items-center gap-3">
            <div class="w-6 h-6 rounded-full bg-greenhouse-400/30 flex items-center justify-center flex-shrink-0">
              <svg class="w-3.5 h-3.5 text-greenhouse-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"/>
              </svg>
            </div>
            <span class="text-greenhouse-100 text-sm">{{ feat.text }}</span>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <p class="relative text-greenhouse-400 text-xs">
        © {{ new Date().getFullYear() }} Greenhouse Management System
      </p>
    </div>

    <!-- ── Right panel — login form ───────────────────────────── -->
    <div class="flex-1 flex flex-col items-center justify-center p-8 bg-white">
      <!-- Mobile logo (shown only on small screens) -->
      <div class="lg:hidden flex items-center gap-3 mb-10">
        <div class="w-10 h-10 bg-greenhouse-700 rounded-2xl flex items-center justify-center text-xl">🌱</div>
        <p class="text-greenhouse-900 font-bold text-lg">Greenhouse</p>
      </div>

      <div class="w-full max-w-sm">
        <!-- Header -->
        <div class="mb-8">
          <h1 class="text-2xl font-bold text-[#1A1A1A]">{{ $t('auth.welcome') }}</h1>
          <p class="text-[#555555] text-sm mt-1.5">{{ $t('auth.login') }}</p>
        </div>

        <!-- Google button -->
        <!--
          The href MUST be an absolute URL pointing to the Spring Boot backend.
          Spring Security sets the session cookie on localhost:8080; using an
          absolute URL ensures the full OAuth2 handshake happens on that origin
          so the cookie is correctly issued for subsequent /api/** calls.
        -->
        <a
          href="http://localhost:8080/oauth2/authorization/google"
          data-testid="google-login-btn"
          class="flex items-center justify-center gap-3 w-full
                 border-2 border-gray-200 hover:border-greenhouse-400 hover:bg-greenhouse-50
                 rounded-xl px-5 py-3.5 font-semibold text-[#1A1A1A] text-sm
                 transition-all duration-200 shadow-sm hover:shadow-md group"
        >
          <!-- Google G logo -->
          <svg class="w-5 h-5 flex-shrink-0" viewBox="0 0 24 24">
            <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
            <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
            <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
            <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
          </svg>
          {{ $t('auth.loginWith') }}
        </a>

        <!-- Locale toggle -->
        <div class="mt-6 text-center">
          <button
            @click="toggleLocale"
            class="text-xs text-gray-400 hover:text-greenhouse-700 transition-colors font-medium"
          >
            {{ locale === 'es' ? 'Switch to English 🇺🇸' : 'Cambiar a Español 🇪🇸' }}
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'

const { locale } = useI18n()

function toggleLocale() {
  const next = locale.value === 'es' ? 'en' : 'es'
  locale.value = next
  localStorage.setItem('locale', next)
}

const features = [
  { text: 'Real-time sensor monitoring' },
  { text: 'Automated rule-based responses' },
  { text: 'Multi-zone greenhouse management' }
]
</script>
