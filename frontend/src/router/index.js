import { createRouter, createWebHistory } from 'vue-router'
import { canAccessPath } from '../utils/navigation'
import { readSessionUser } from '../utils/sessionUser'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue'), meta: { public: true } },
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', component: () => import('../views/Dashboard.vue') },
  { path: '/documents', component: () => import('../views/Documents.vue') },
  { path: '/seals', component: () => import('../views/Seals.vue') },
  { path: '/meetings', component: () => import('../views/Meetings.vue') },
  { path: '/mails', component: () => import('../views/Mails.vue') },
  { path: '/travels', component: () => import('../views/Travels.vue') },
  { path: '/reports', component: () => import('../views/Reports.vue') },
  { path: '/approvals', component: () => import('../views/Approvals.vue') },
  { path: '/announcements', component: () => import('../views/Announcements.vue') },
  { path: '/statistics', component: () => import('../views/Statistics.vue') },
  { path: '/admin/users', component: () => import('../views/UserManage.vue') },
  { path: '/admin/dictionaries', component: () => import('../views/DictionaryManage.vue') },
  { path: '/:pathMatch(.*)*', component: () => import('../views/NotFound.vue'), meta: { public: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('oms_token')
  if (!to.meta.public && !token) {
    next('/login')
  } else if (!to.meta.public) {
    const user = readSessionUser()
    if (canAccessPath(to.path, user?.roleKeys || [])) {
      next()
    } else {
      next('/dashboard')
    }
  } else {
    next()
  }
})

export default router
