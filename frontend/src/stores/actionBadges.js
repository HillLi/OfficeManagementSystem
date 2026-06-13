// 待办角标状态管理，负责各业务模块未处理数量的统计与展示
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
  // 待办任务数量
  const pendingTasks = ref(0)
  // 未读通知数量
  const unreadNotifications = ref(0)
  // 各菜单路由对应的角标数量映射
  const menuCounts = ref({})

  // 计算属性：审批模块的角标总数
  const approvalTotal = computed(() => approvalBadgeCount({
    pendingTasks: pendingTasks.value,
    unreadNotifications: unreadNotifications.value
  }))

  // 根据任务和通知数据设置基础角标数量
  function setFromData(tasks = [], notifications = []) {
    pendingTasks.value = pendingTaskCount(tasks)
    unreadNotifications.value = actionableNotificationCount(notifications)
    menuCounts.value = {
      ...menuCounts.value,
      '/approvals': approvalTotal.value
    }
  }

  // 批量加载公文分发数据
  async function loadDocumentDistributions(documents = []) {
    const distributionGroups = await Promise.all(
      documents.map((document) => api.documentDistributions(document.id).catch(() => []))
    )
    return distributionGroups.flat()
  }

  // 刷新所有业务模块的角标数据
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

  // 获取指定路由对应的角标数量
  const menuBadgeFor = (route) => menuCounts.value[route] || 0

  // 重置所有角标数据
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
