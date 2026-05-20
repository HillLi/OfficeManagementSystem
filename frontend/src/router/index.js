import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '../views/Dashboard.vue'
import Documents from '../views/Documents.vue'
import Seals from '../views/Seals.vue'
import Meetings from '../views/Meetings.vue'
import Travels from '../views/Travels.vue'
import Reports from '../views/Reports.vue'
import Approvals from '../views/Approvals.vue'
import Login from '../views/Login.vue'

const routes = [
  { path: '/login', component: Login, meta: { public: true } },
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', component: Dashboard },
  { path: '/documents', component: Documents },
  { path: '/seals', component: Seals },
  { path: '/meetings', component: Meetings },
  { path: '/travels', component: Travels },
  { path: '/reports', component: Reports },
  { path: '/approvals', component: Approvals }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('oms_token')
  if (!to.meta.public && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
