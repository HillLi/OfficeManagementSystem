const INITIATOR_ROLES = ['office_user', 'dept_head', 'school_leader', 'office_admin']
const APPROVAL_ROLES = ['dept_head', 'school_leader', 'office_admin', 'finance_staff', 'security_staff']

export const menuItems = [
  { index: '/dashboard', label: '工作台' },
  { index: '/documents', label: '公文管理', roles: INITIATOR_ROLES },
  { index: '/seals', label: '印章管理', roles: [...INITIATOR_ROLES, 'seal_keeper'] },
  { index: '/meetings', label: '会议管理', roles: [...INITIATOR_ROLES, 'security_staff'] },
  { index: '/travels', label: '差旅审批', roles: ['office_user', 'dept_head', 'school_leader', 'finance_staff'] },
  { index: '/reports', label: '请示报告', roles: INITIATOR_ROLES },
  { index: '/approvals', label: '审批任务', roles: APPROVAL_ROLES },
  { index: '/announcements', label: '通知公告' },
  { index: '/statistics', label: '统计报表' },
  { index: '/admin/users', label: '用户管理', roles: ['admin'] },
  { index: '/admin/dictionaries', label: '字典管理', roles: ['admin'] }
]

function hasAllowedRole(item, roleKeys) {
  return !item.roles || item.roles.some((role) => roleKeys.includes(role))
}

export function visibleMenuItems(roleKeys = []) {
  return menuItems.filter((item) => hasAllowedRole(item, roleKeys))
}

export function canAccessPath(path, roleKeys = []) {
  const item = menuItems.find((candidate) => candidate.index === path)
  return !item || hasAllowedRole(item, roleKeys)
}
