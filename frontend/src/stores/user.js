import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { readSessionUser, writeSessionUser } from '../utils/sessionUser'

export const useUserStore = defineStore('user', () => {
  const user = ref(readSessionUser())
  const token = ref(sessionStorage.getItem('oms_token') || '')

  const isLoggedIn = computed(() => user.value != null)
  const realName = computed(() => user.value?.realName || '')
  const roleKeys = computed(() => user.value?.roleKeys || [])

  function setUser(u, t) {
    user.value = u
    token.value = t
    writeSessionUser(u)
    sessionStorage.setItem('oms_token', t)
  }

  function logout() {
    user.value = null
    token.value = ''
    sessionStorage.removeItem('oms_user')
    sessionStorage.removeItem('oms_token')
  }

  return { user, token, isLoggedIn, realName, roleKeys, setUser, logout }
})
