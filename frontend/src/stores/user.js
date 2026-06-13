// 用户状态管理，负责用户登录信息、令牌及角色的存储与操作
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { readSessionUser, writeSessionUser } from '../utils/sessionUser'

export const useUserStore = defineStore('user', () => {
  // 当前登录用户信息
  const user = ref(readSessionUser())
  // 当前用户的认证令牌
  const token = ref(sessionStorage.getItem('oms_token') || '')

  // 计算属性：是否已登录
  const isLoggedIn = computed(() => user.value != null)
  // 计算属性：获取用户真实姓名
  const realName = computed(() => user.value?.realName || '')
  // 计算属性：获取用户角色标识列表
  const roleKeys = computed(() => user.value?.roleKeys || [])

  // 设置用户信息和令牌，并持久化到会话存储
  function setUser(u, t) {
    user.value = u
    token.value = t
    writeSessionUser(u)
    sessionStorage.setItem('oms_token', t)
  }

  // 退出登录，清除用户信息和令牌
  function logout() {
    user.value = null
    token.value = ''
    sessionStorage.removeItem('oms_user')
    sessionStorage.removeItem('oms_token')
  }

  return { user, token, isLoggedIn, realName, roleKeys, setUser, logout }
})
