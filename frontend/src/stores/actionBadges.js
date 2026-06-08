import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { api } from '../api'
import {
  actionableNotificationCount,
  approvalBadgeCount,
  businessActionCounts,
  pendingTaskCount
} from '../utils/actionBadges'

export const useActionBadgeStore = defineStore('actionBadges', () => {
  const pendingTasks = ref(0)
  const unreadNotifications = ref(0)
  const menuCounts = ref({})

  const approvalTotal = computed(() => approvalBadgeCount({
    pendingTasks: pendingTasks.value,
    unreadNotifications: unreadNotifications.value
  }))

  function setFromData(tasks = [], notifications = []) {
    pendingTasks.value = pendingTaskCount(tasks)
    unreadNotifications.value = actionableNotificationCount(notifications)
    menuCounts.value = {
      ...menuCounts.value,
      '/approvals': approvalTotal.value
    }
  }

  async function loadDocumentDistributions(documents = []) {
    const distributionGroups = await Promise.all(
      documents.map((document) => api.documentDistributions(document.id).catch(() => []))
    )
    return distributionGroups.flat()
  }

  async function refresh(currentUser = null) {
    const [tasks, notifications] = await Promise.all([
      api.flowTasks({ onlyMine: true }),
      api.notifications({ unreadOnly: true })
    ])
    const [
      mailInbox,
      documents,
      sealApplications,
      meetings,
      participatedMeetings,
      travels,
      reports
    ] = await Promise.all([
      api.mailInbox().catch(() => []),
      api.documents().catch(() => []),
      api.sealApps().catch(() => []),
      api.meetings().catch(() => []),
      api.meetingsParticipated().catch(() => []),
      api.travels().catch(() => []),
      api.reports().catch(() => [])
    ])
    const documentDistributions = await loadDocumentDistributions(documents)
    setFromData(tasks, notifications)
    menuCounts.value = businessActionCounts({
      currentUser,
      tasks,
      notifications,
      mailInbox,
      documents,
      documentDistributions,
      sealApplications,
      meetings,
      participatedMeetings,
      travels,
      reports
    })
  }

  const menuBadgeFor = (route) => menuCounts.value[route] || 0

  function reset() {
    pendingTasks.value = 0
    unreadNotifications.value = 0
    menuCounts.value = {}
  }

  return {
    pendingTasks,
    unreadNotifications,
    approvalTotal,
    menuCounts,
    menuBadgeFor,
    setFromData,
    refresh,
    reset
  }
})
