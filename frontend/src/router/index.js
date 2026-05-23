import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/components/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/dashboard'
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/DashboardView.vue')
      },
      {
        path: 'greenhouses',
        name: 'Greenhouses',
        component: () => import('@/views/GreenhouseView.vue'),
        meta: { roles: ['ADMIN'] }
      },
      {
        path: 'zones',
        name: 'Zones',
        component: () => import('@/views/ZoneView.vue'),
        meta: { roles: ['ADMIN'] }
      },
      {
        path: 'sensors',
        name: 'Sensors',
        component: () => import('@/views/SensorView.vue'),
        meta: { roles: ['ADMIN'] }
      },
      {
        path: 'actuators',
        name: 'Actuators',
        component: () => import('@/views/ActuatorView.vue'),
        meta: { roles: ['ADMIN'] }
      },
      {
        path: 'automation-rules',
        name: 'AutomationRules',
        component: () => import('@/views/AutomationRuleView.vue'),
        meta: { roles: ['ADMIN'] }
      },
      {
        path: 'alerts',
        name: 'Alerts',
        component: () => import('@/views/AlertView.vue'),
        meta: { roles: ['ADMIN', 'OPERATOR'] }
      },
      {
        path: 'sensor-readings',
        name: 'SensorReadings',
        component: () => import('@/views/SensorReadingView.vue'),
        meta: { roles: ['ADMIN', 'OPERATOR'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * Global navigation guard.
 *
 * Made async so it can await authStore.init() before making any routing
 * decision. init() is idempotent — only the very first navigation in a
 * browser session actually calls the backend; subsequent guards return
 * immediately from the cached initialized flag.
 *
 * Decision tree:
 *  1. Call init() to ensure session state is known.
 *  2. Authenticated user → /login: redirect to /dashboard (already logged in).
 *  3. Public route (requiresAuth: false): allow.
 *  4. Protected route + not authenticated: redirect to /login.
 *  5. Role-restricted route + wrong role: redirect to /dashboard.
 *  6. All other cases: allow.
 */
router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()

  // Verify session with backend on the first navigation of the session.
  await authStore.init()

  // Already logged in — no reason to show the login page.
  if (to.name === 'Login' && authStore.isAuthenticated) {
    return next('/dashboard')
  }

  // Public route — always allow.
  if (to.meta.requiresAuth === false) {
    return next()
  }

  // Protected route — must be authenticated.
  if (!authStore.isAuthenticated) {
    return next('/login')
  }

  // Role-restricted route — must have the required role.
  if (to.meta.roles && !to.meta.roles.includes(authStore.user?.role)) {
    return next('/dashboard')
  }

  next()
})

export default router
