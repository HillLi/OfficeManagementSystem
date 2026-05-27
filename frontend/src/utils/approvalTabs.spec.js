import { describe, expect, it } from 'vitest'
import { approvalTabs, defaultApprovalTab } from './approvalTabs'

describe('approval tabs', () => {
  it('keeps approval page sections in task-first tab order', () => {
    expect(defaultApprovalTab).toBe('tasks')
    expect(approvalTabs).toEqual([
      { name: 'tasks', label: '待办任务' },
      { name: 'notifications', label: '通知提醒' },
      { name: 'instances', label: '流程实例' },
      { name: 'history', label: '审批记录' }
    ])
  })
})
