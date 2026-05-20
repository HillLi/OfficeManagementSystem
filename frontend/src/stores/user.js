import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse(sessionStorage.getItem('oms_user') || 'null'))
  const token = ref(sessionStorage.getItem('oms_token') || '')

  const isLoggedIn = computed(() => user.value != null)
  const realName = computed(() => user.value?.realName || '')
  const roleKeys = computed(() => user.value?.roleKeys || [])

  function setUser(u, t) {
    user.value = u
    token.value = t
    sessionStorage.setItem('oms_user', JSON.stringify(u))
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
