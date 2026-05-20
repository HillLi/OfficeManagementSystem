<template>
  <div class="login-wrapper">
    <div class="login-card panel">
      <h2 style="text-align:center;color:#1f5f8b">高校办公管理系统</h2>
      <el-form label-position="top" @submit.prevent="doLogin">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-button type="primary" style="width:100%" @click="doLogin" :loading="loading">登录</el-button>
      </el-form>
      <p v-if="error" style="color:red;margin-top:10px">{{ error }}</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { api } from '../api'

const emit = defineEmits(['login-success'])
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const error = ref('')
const form = reactive({ username: 'user', password: '123456' })

const doLogin = async () => {
  loading.value = true
  error.value = ''
  try {
    const result = await api.login(form)
    userStore.setUser(result.user, result.token)
    emit('login-success')
    router.push('/dashboard')
  } catch (e) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f4f7fb;
}
.login-card {
  width: 380px;
}
</style>
