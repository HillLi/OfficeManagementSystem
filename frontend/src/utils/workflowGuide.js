// 工作流节点状态与类型的展示映射

/** 工作流节点状态的文字和标签类型映射 */
export const workflowStatusMeta = {
  done: { text: '已完成', type: 'success' },
  current: { text: '当前节点', type: 'warning' },
  waiting: { text: '待办理', type: 'info' },
  rejected: { text: '已退回', type: 'danger' },
  optional: { text: '可选', type: 'info' }
}

/** 工作流节点类型的中文映射 */
export const workflowTypeText = {
  business: '业务办理',
  approval: '审批',
  system: '系统校验'
}

/** 获取节点状态的中文文本 */
export function statusText(status) {
  return workflowStatusMeta[status]?.text || status || '-'
}

/** 获取节点状态对应的标签类型（用于颜色渲染） */
export function statusTagType(status) {
  return workflowStatusMeta[status]?.type || 'info'
}

/** 生成工作流节点的 CSS 类名 */
export function stepClass(step) {
  return ['flow-step', `is-${step?.status || 'waiting'}`]
}

/** 获取节点类型的中文文本 */
export function typeText(type) {
  return workflowTypeText[type] || type || '-'
}
