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
            {{ item.label }}
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
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { api } from './api'
import { useUserStore } from './stores/user'
import { useDictionaryStore } from './stores/dictionary'
import { visibleMenuItems } from './utils/navigation'
import Login from './views/Login.vue'

const router = useRouter()
const userStore = useUserStore()
const dictionaryStore = useDictionaryStore()
const visibleItems = computed(() => visibleMenuItems(userStore.roleKeys))

onMounted(async () => {
  if (userStore.isLoggedIn) {
    try {
      await dictionaryStore.refresh()
    } catch (error) {
      dictionaryStore.restoreCached()
    }
  }
})

const handleLogout = async () => {
  try {
    await api.logout()
  } finally {
    userStore.logout()
    dictionaryStore.restoreCached()
    router.push('/login')
  }
}
</script>
