<template>
  <div class="login-wrapper">
    <div class="login-card panel">
      <h2 style="text-align:center;color:#1f5f8b">高校办公管理系统</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="doLogin">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
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
import { useDictionaryStore } from '../stores/dictionary'
import { api } from '../api'

const emit = defineEmits(['login-success'])
const router = useRouter()
const userStore = useUserStore()
const dictionaryStore = useDictionaryStore()
const loading = ref(false)
const error = ref('')
const formRef = ref(null)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const doLogin = async () => {
  loading.value = true
  error.value = ''
  try {
    await formRef.value.validate()
    const result = await api.login(form)
    userStore.setUser(result.user, result.token)
    try {
      await dictionaryStore.refresh(true)
    } catch (refreshError) {
      userStore.logout()
      throw refreshError
    }
    emit('login-success')
    router.push('/dashboard')
  } catch (e) {
    if (e.message !== 'validation failed') {
      error.value = e.message || '登录失败'
    }
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
