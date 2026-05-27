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
          <el-menu-item index="/dashboard">工作台</el-menu-item>
          <el-menu-item index="/documents">公文管理</el-menu-item>
          <el-menu-item index="/seals">印章管理</el-menu-item>
          <el-menu-item index="/meetings">会议管理</el-menu-item>
          <el-menu-item index="/travels">差旅审批</el-menu-item>
          <el-menu-item index="/reports">请示报告</el-menu-item>
          <el-menu-item index="/approvals">审批任务</el-menu-item>
          <el-menu-item index="/statistics">统计报表</el-menu-item>
          <el-menu-item v-if="isAdmin" index="/admin/users">用户管理</el-menu-item>
          <el-menu-item v-if="isAdmin" index="/admin/dictionaries">字典管理</el-menu-item>
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
import Login from './views/Login.vue'

const router = useRouter()
const userStore = useUserStore()
const dictionaryStore = useDictionaryStore()
const isAdmin = computed(() => userStore.roleKeys.includes('admin'))

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
