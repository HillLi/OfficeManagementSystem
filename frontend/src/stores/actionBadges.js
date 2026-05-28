import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { api } from '../api'
import { actionableNotificationCount, approvalBadgeCount, pendingTaskCount } from '../utils/actionBadges'

export const useActionBadgeStore = defineStore('actionBadges', () => {
  const pendingTasks = ref(0)
  const unreadNotifications = ref(0)

  const approvalTotal = computed(() => approvalBadgeCount({
    pendingTasks: pendingTasks.value,
    unreadNotifications: unreadNotifications.value
  }))

  function setFromData(tasks = [], notifications = []) {
    pendingTasks.value = pendingTaskCount(tasks)
    unreadNotifications.value = actionableNotificationCount(notifications)
  }

  async function refresh() {
    const [tasks, notifications] = await Promise.all([
      api.flowTasks({ onlyMine: true }),
      api.notifications({ unreadOnly: true })
    ])
    setFromData(tasks, notifications)
  }

  function reset() {
    pendingTasks.value = 0
    unreadNotifications.value = 0
  }

  return { pendingTasks, unreadNotifications, approvalTotal, setFromData, refresh, reset }
})
