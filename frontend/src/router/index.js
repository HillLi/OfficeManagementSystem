// 路由配置模块：定义页面路径映射及导航守卫
import { createRouter, createWebHistory } from 'vue-router'
import { canAccessPath } from '../utils/navigation'
import { readSessionUser } from '../utils/sessionUser'

// 所有页面路由配置（懒加载方式引入组件）
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

// 创建路由实例，使用 HTML5 History 模式
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫：校验登录状态和页面访问权限
router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('oms_token')
  if (!to.meta.public && !token) {
    // 未登录且访问非公开页面，跳转到登录页
    next('/login')
  } else if (!to.meta.public) {
    // 已登录，检查用户角色是否有权访问目标路径
    const user = readSessionUser()
    if (canAccessPath(to.path, user?.roleKeys || [])) {
      next()
    } else {
      // 无权限则重定向到仪表盘
      next('/dashboard')
    }
  } else {
    next()
  }
})

export default router
