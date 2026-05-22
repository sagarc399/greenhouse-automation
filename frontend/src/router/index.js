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

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth === false) {
    return next()
  }

  if (!authStore.isAuthenticated) {
    return next('/login')
  }

  if (to.meta.roles && !to.meta.roles.includes(authStore.user?.role)) {
    return next('/dashboard')
  }

  next()
})

export default router
