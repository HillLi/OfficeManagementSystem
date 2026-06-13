// 徽章计数与菜单角标计算

/** 统计待办任务的数量 */
export function pendingTaskCount(tasks = []) {
  return tasks.filter((task) => task.status === 'pending').length
}

/** 统计未读通知的数量 */
export function actionableNotificationCount(notifications = []) {
  return notifications.filter((notification) => !notification.readStatus).length
}

/** 计算审批模块的总角标数（待办 + 未读通知） */
export function approvalBadgeCount(counts = {}) {
  return (counts.pendingTasks || 0) + (counts.unreadNotifications || 0)
}

/** 根据标签页名称获取对应的角标数量 */
export function approvalTabBadgeCount(tabName, counts = {}) {
  if (tabName === 'tasks') return counts.pendingTasks || 0
  if (tabName === 'notifications') return counts.unreadNotifications || 0
  return 0
}

/** 业务类型到菜单路由的映射 */
const BIZ_ROUTE = {
  document: '/documents',
  seal: '/seals',
  meeting: '/meetings',
  travel: '/travels',
  report: '/reports'
}

/** 创建菜单路由的初始计数对象（全部归零） */
function emptyMenuCounts() {
  return {
    '/documents': 0,
    '/seals': 0,
    '/meetings': 0,
    '/mails': 0,
    '/travels': 0,
    '/reports': 0,
    '/approvals': 0
  }
}

/** 判断用户是否拥有指定角色 */
function hasRole(currentUser, role) {
  return currentUser?.roleKeys?.includes(role)
}

/** 判断用户是否可以管理办公室业务（管理员或办公室人员） */
function canManageOfficeWork(currentUser) {
  return hasRole(currentUser, 'admin') || hasRole(currentUser, 'office_admin')
}

/** 判断用户是否可以管理印章 */
function canManageSeals(currentUser) {
  return canManageOfficeWork(currentUser) || hasRole(currentUser, 'seal_keeper')
}

/** 对指定路由累加计数 */
function add(counts, route, amount = 1) {
  if (!route || amount <= 0) return
  counts[route] = (counts[route] || 0) + amount
}

/** 根据当前用户和各项业务数据计算各菜单的角标数量 */
export function businessActionCounts({
  currentUser = null,
  tasks = [],
  notifications = [],
  mailInbox = [],
  documents = [],
  documentDistributions = [],
  sealApplications = [],
  meetings = [],
  participatedMeetings = [],
  travels = [],
  reports = []
} = {}) {
  const counts = emptyMenuCounts()
  const pendingTasks = tasks.filter((task) => task.status === 'pending')
  const unreadNotifications = actionableNotificationCount(notifications)

  pendingTasks.forEach((task) => add(counts, BIZ_ROUTE[task.bizType]))
  counts['/approvals'] = pendingTasks.length + unreadNotifications

  add(counts, '/mails', mailInbox.filter((mail) => mail.currentUserRead === false).length)

  add(counts, '/documents', documents.filter((doc) => (
    canManageOfficeWork(currentUser) && doc.status === 'approved'
  )).length)
  add(counts, '/documents', documentDistributions.filter((distribution) => (
    distribution.receiverId === currentUser?.id && distribution.status !== 'received'
  )).length)

  add(counts, '/seals', sealApplications.filter((application) => (
    canManageSeals(currentUser) && ['approved', 'used'].includes(application.status)
  )).length)

  add(counts, '/meetings', meetings.filter((meeting) => (
    meeting.status === 'approved' && meeting.recorderId === currentUser?.id
  )).length)
  add(counts, '/meetings', meetings.filter((meeting) => (
    meeting.status === 'minutes_confirmed' && meeting.organizerId === currentUser?.id
  )).length)
  add(counts, '/meetings', participatedMeetings.filter((meeting) => (
    meeting.status === 'minutes_pending' && !meeting.minutesConfirmed
  )).length)

  add(counts, '/travels', travels.filter((travel) => (
    travel.applicantId === currentUser?.id
    && travel.status === 'approved'
    && !travel.reimbursementSubmitted
  )).length)

  add(counts, '/reports', reports.filter((report) => (
    canManageOfficeWork(currentUser) && report.status === 'approved'
  )).length)

  return counts
}
