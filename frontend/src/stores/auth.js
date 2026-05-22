import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

/**
 * Pinia store for authentication state.
 * Holds the current user and exposes login/logout helpers.
 */
export const useAuthStore = defineStore('auth', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isAuthenticated = computed(() => user.value !== null)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const isOperator = computed(() => user.value?.role === 'OPERATOR' || isAdmin.value)

  function setUser(userData) {
    user.value = userData
    localStorage.setItem('user', JSON.stringify(userData))
  }

  async function fetchMe() {
    try {
      const { data } = await api.get('/api/me')
      setUser(data)
    } catch {
      logout()
    }
  }

  function logout() {
    user.value = null
    localStorage.removeItem('user')
    window.location.href = '/logout'
  }

  return { user, isAuthenticated, isAdmin, isOperator, setUser, fetchMe, logout }
})
