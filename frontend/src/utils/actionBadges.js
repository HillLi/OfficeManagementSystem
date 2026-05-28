export function pendingTaskCount(tasks = []) {
  return tasks.filter((task) => task.status === 'pending').length
}

export function actionableNotificationCount(notifications = []) {
  return notifications.filter((notification) => !notification.readStatus).length
}

export function approvalBadgeCount(counts = {}) {
  return (counts.pendingTasks || 0) + (counts.unreadNotifications || 0)
}

export function approvalTabBadgeCount(tabName, counts = {}) {
  if (tabName === 'tasks') return counts.pendingTasks || 0
  if (tabName === 'notifications') return counts.unreadNotifications || 0
  return 0
}
