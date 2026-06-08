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

const BIZ_ROUTE = {
  document: '/documents',
  seal: '/seals',
  meeting: '/meetings',
  travel: '/travels',
  report: '/reports'
}

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

function hasRole(currentUser, role) {
  return currentUser?.roleKeys?.includes(role)
}

function canManageOfficeWork(currentUser) {
  return hasRole(currentUser, 'admin') || hasRole(currentUser, 'office_admin')
}

function canManageSeals(currentUser) {
  return canManageOfficeWork(currentUser) || hasRole(currentUser, 'seal_keeper')
}

function add(counts, route, amount = 1) {
  if (!route || amount <= 0) return
  counts[route] = (counts[route] || 0) + amount
}

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
