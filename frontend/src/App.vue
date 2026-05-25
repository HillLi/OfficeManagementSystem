<template>
  <Login v-if="$route.path === '/login'" />
  <div v-else class="layout">
    <header class="header">
      <div class="brand">高校办公管理系统</div>
      <div>
        当前用户：{{ userStore.realName }}
        <el-button size="small" style="margin-left:12px" @click="handleLogout">退出</el-button>
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
        </el-menu>
      </aside>
      <section class="content">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { api } from './api'
import { useUserStore } from './stores/user'
import Login from './views/Login.vue'

const router = useRouter()
const userStore = useUserStore()
const isAdmin = computed(() => userStore.roleKeys.includes('admin'))

const handleLogout = async () => {
  try {
    await api.logout()
  } finally {
    userStore.logout()
    router.push('/login')
  }
}
</script>
