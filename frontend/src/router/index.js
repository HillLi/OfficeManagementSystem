import { createRouter, createWebHistory } from 'vue-router'
import Approvals from '../views/Approvals.vue'
import Announcements from '../views/Announcements.vue'
import Dashboard from '../views/Dashboard.vue'
import Documents from '../views/Documents.vue'
import Login from '../views/Login.vue'
import Meetings from '../views/Meetings.vue'
import Reports from '../views/Reports.vue'
import Seals from '../views/Seals.vue'
import Statistics from '../views/Statistics.vue'
import Travels from '../views/Travels.vue'
import UserManage from '../views/UserManage.vue'
import DictionaryManage from '../views/DictionaryManage.vue'
import { canAccessPath } from '../utils/navigation'

const routes = [
  { path: '/login', component: Login, meta: { public: true } },
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', component: Dashboard },
  { path: '/documents', component: Documents },
  { path: '/seals', component: Seals },
  { path: '/meetings', component: Meetings },
  { path: '/travels', component: Travels },
  { path: '/reports', component: Reports },
  { path: '/approvals', component: Approvals },
  { path: '/announcements', component: Announcements },
  { path: '/statistics', component: Statistics },
  { path: '/admin/users', component: UserManage },
  { path: '/admin/dictionaries', component: DictionaryManage }
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
    const user = JSON.parse(sessionStorage.getItem('oms_user') || 'null')
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
