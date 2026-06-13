<template>
  <Login v-if="$route.path === '/login'" />
  <div v-else class="layout">
    <header class="header">
      <div class="brand">高校办公管理系统</div>
      <div class="account">
        当前用户：{{ userStore.realName }}
        <el-button size="small" @click="handleLogout">退出</el-button>
      </div>
    </header>
    <main class="main">
      <aside class="side">
        <el-menu :default-active="$route.path" router>
          <el-menu-item v-for="item in visibleItems" :key="item.index" :index="item.index">
            <span class="menu-label">
              {{ item.label }}
              <span v-if="menuBadge(item) > 0" class="menu-badge-count">{{ menuBadge(item) }}</span>
            </span>
          </el-menu-item>
        </el-menu>
      </aside>
      <section class="content">
        <router-view />
      </section>
    </main>
  </div>
</template>

// 应用根组件：提供登录页面和主布局（头部、侧边菜单、内容区）的切换
<script setup>
import { computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from './api'
import { useUserStore } from './stores/user'
import { useDictionaryStore } from './stores/dictionary'
import { useActionBadgeStore } from './stores/actionBadges'
import { visibleMenuItems } from './utils/navigation'
import Login from './views/Login.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const dictionaryStore = useDictionaryStore()
const actionBadgeStore = useActionBadgeStore()

// 根据用户角色计算可见菜单项
const visibleItems = computed(() => visibleMenuItems(userStore.roleKeys))
let refreshTimer = null

// 获取菜单项的角标数量
const menuBadge = (item) => actionBadgeStore.menuBadgeFor(item.index)

// 业务操作事件回调，触发Shell数据刷新
const handleBusinessAction = () => refreshShellData()

// 刷新字典和角标等Shell级别数据
const refreshShellData = async () => {
  if (!userStore.isLoggedIn) {
    actionBadgeStore.reset()
    return
  }
  try {
    await dictionaryStore.refresh()
  } catch (error) {
    dictionaryStore.restoreCached()
  }
  try {
    await actionBadgeStore.refresh(userStore.user)
  } catch (error) {
    actionBadgeStore.reset()
  }
}

// 组件挂载时加载数据，注册定时刷新和业务事件监听
onMounted(() => {
  refreshShellData()
  window.addEventListener('oms:business-action', handleBusinessAction)
  refreshTimer = window.setInterval(refreshShellData, 30000)
})

// 组件卸载时清理事件监听和定时器
onUnmounted(() => {
  window.removeEventListener('oms:business-action', handleBusinessAction)
  if (refreshTimer) {
    window.clearInterval(refreshTimer)
  }
})

// 监听登录状态和路由变化，刷新Shell数据
watch(() => [userStore.isLoggedIn, route.path], refreshShellData)

// 处理用户退出登录
const handleLogout = async () => {
  try {
    await api.logout()
  } finally {
    userStore.logout()
    dictionaryStore.restoreCached()
    actionBadgeStore.reset()
    router.push('/login')
  }
}
</script>

<style scoped>
.menu-label {
  width: 100%;
  display: inline-flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.menu-badge-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  border-radius: 999px;
  background: #f56c6c;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  line-height: 1;
}
</style>
