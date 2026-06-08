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
const visibleItems = computed(() => visibleMenuItems(userStore.roleKeys))
let refreshTimer = null

const menuBadge = (item) => actionBadgeStore.menuBadgeFor(item.index)
const handleBusinessAction = () => refreshShellData()

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

onMounted(() => {
  refreshShellData()
  window.addEventListener('oms:business-action', handleBusinessAction)
  refreshTimer = window.setInterval(refreshShellData, 30000)
})
onUnmounted(() => {
  window.removeEventListener('oms:business-action', handleBusinessAction)
  if (refreshTimer) {
    window.clearInterval(refreshTimer)
  }
})
watch(() => [userStore.isLoggedIn, route.path], refreshShellData)

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
