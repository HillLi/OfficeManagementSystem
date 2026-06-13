<template>
  <Login v-if="$route.path === '/login'" />
  <div v-else class="layout">
    <header class="header">
      <div class="brand">高校办公管理系统</div>
      <div class="account">
        <el-dropdown trigger="click">
          <span class="user-link">
            {{ userStore.realName }} <el-icon><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="showChangePassword = true">修改密码</el-dropdown-item>
              <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
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

  <!-- 修改密码对话框 -->
  <el-dialog v-model="showChangePassword" title="修改密码" width="420px" :close-on-click-modal="false">
    <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showChangePassword = false">取消</el-button>
      <el-button type="primary" :loading="pwdSaving" @click="handleChangePassword">确定</el-button>
    </template>
  </el-dialog>
</template>

// 应用根组件：提供登录页面和主布局（头部、侧边菜单、内容区）的切换
<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
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

// 修改密码相关
const showChangePassword = ref(false)
const pwdSaving = ref(false)
const pwdFormRef = ref(null)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

// 确认密码校验
const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

// 提交修改密码
const handleChangePassword = async () => {
  try {
    await pwdFormRef.value.validate()
  } catch (e) {
    if (e.message !== 'validation failed') throw e
    return
  }
  pwdSaving.value = true
  try {
    await api.changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功')
    showChangePassword.value = false
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch (e) {
    ElMessage.error(e.message || '密码修改失败')
  } finally {
    pwdSaving.value = false
  }
}
</script>

<style scoped>
.user-link {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #fff;
  font-size: 14px;
}
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
