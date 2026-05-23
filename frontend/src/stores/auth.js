import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/services/api'

/**
 * Pinia store for authentication state.
 *
 * Auth state is derived entirely from the Spring Security session (JSESSIONID
 * cookie). There is intentionally no localStorage caching: on every cold load
 * or page refresh, init() calls fetchMe() to ask the backend whether the
 * session is still valid.
 *
 * Flow:
 *   App mount → router.beforeEach → authStore.init() → GET /api/user/me
 *     200: user is authenticated, set user data, allow navigation
 *     401: no active session, user = null, redirect to /login
 */
export const useAuthStore = defineStore('auth', () => {
  /** Authenticated user data, or null if not logged in. */
  const user = ref(null)

  /**
   * True after the first fetchMe() call completes.
   * Prevents duplicate network calls when multiple route guards fire.
   */
  const initialized = ref(false)

  /**
   * In-flight fetchMe() Promise.
   * Stored so concurrent init() callers await the same request instead of
   * each firing their own — guards the window between "initialized = true"
   * and the actual network response arriving.
   *
   * @type {Promise<void> | null}
   */
  let initPromise = null

  /** True when the user has an active session. */
  const isAuthenticated = computed(() => user.value !== null)

  /** True when the user has the ADMIN role. */
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  /** True when the user has OPERATOR or ADMIN role. */
  const isOperator = computed(() => user.value?.role === 'OPERATOR' || isAdmin.value)

  /**
   * Initialises auth state by verifying the session with the backend.
   * Safe to call multiple times — only the first call makes a network request.
   *
   * Concurrent callers (e.g. two route guards firing before the first response
   * arrives) all await the same in-flight Promise instead of racing.
   */
  async function init() {
    if (initialized.value) return
    initialized.value = true
    initPromise = fetchMe()
    await initPromise
  }

  /**
   * Fetches the current user from GET /api/user/me.
   *
   * Uses the _isAuthCheck flag to tell the Axios interceptor not to
   * redirect on 401 — this store handles that case itself by setting
   * user = null.
   */
  async function fetchMe() {
    console.log('[auth] fetchMe() → calling GET', api.defaults.baseURL + '/api/user/me')
    try {
      const { data } = await api.get('/api/user/me', { _isAuthCheck: true })
      console.log('[auth] fetchMe() ✓ response:', data)
      user.value = data
    } catch (err) {
      console.warn(
        '[auth] fetchMe() ✗ error:',
        err.response?.status,
        err.response?.data ?? err.message
      )
      user.value = null
    }
  }

  /**
   * Logs the user out by hitting Spring Security's /logout endpoint.
   * Spring invalidates the session and redirects the browser to
   * localhost:5173/login (configured in SecurityConfig).
   */
  function logout() {
    user.value = null
    initialized.value = false
    // Full-page navigation to the backend's logout endpoint.
    // Spring Security invalidates the session and redirects back to the
    // frontend login page (app.frontend.base-url + /login).
    window.location.href = 'http://localhost:8080/logout'
  }

  return {
    user,
    isAuthenticated,
    isAdmin,
    isOperator,
    init,
    fetchMe,
    logout
  }
})
