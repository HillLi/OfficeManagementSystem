export const workflowStatusMeta = {
  done: { text: '已完成', type: 'success' },
  current: { text: '当前节点', type: 'warning' },
  waiting: { text: '待办理', type: 'info' },
  rejected: { text: '已退回', type: 'danger' },
  optional: { text: '可选', type: 'info' }
}

export const workflowTypeText = {
  business: '业务办理',
  approval: '审批',
  system: '系统校验'
}

export function statusText(status) {
  return workflowStatusMeta[status]?.text || status || '-'
}

export function statusTagType(status) {
  return workflowStatusMeta[status]?.type || 'info'
}

export function stepClass(step) {
  return ['flow-step', `is-${step?.status || 'waiting'}`]
}

export function typeText(type) {
  return workflowTypeText[type] || type || '-'
}
