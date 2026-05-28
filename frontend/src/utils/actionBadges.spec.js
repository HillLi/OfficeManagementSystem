import { describe, expect, it } from 'vitest'
import { approvalBadgeCount, approvalTabBadgeCount, actionableNotificationCount, pendingTaskCount } from './actionBadges'

describe('action badge counters', () => {
  it('counts only pending approval tasks as actionable', () => {
    expect(pendingTaskCount([
      { id: 1, status: 'pending' },
      { id: 2, status: 'completed' },
      { id: 3, status: 'pending' }
    ])).toBe(2)
  })

  it('counts only unread notifications as actionable', () => {
    expect(actionableNotificationCount([
      { id: 1, readStatus: false },
      { id: 2, readStatus: true },
      { id: 3, readStatus: false }
    ])).toBe(2)
  })

  it('shows approval menu badges as pending tasks plus unread notifications', () => {
    const counts = { pendingTasks: 2, unreadNotifications: 3 }
    expect(approvalBadgeCount(counts)).toBe(5)
  })

  it('shows badges only on tabs that need action', () => {
    const counts = { pendingTasks: 2, unreadNotifications: 3 }
    expect(approvalTabBadgeCount('tasks', counts)).toBe(2)
    expect(approvalTabBadgeCount('notifications', counts)).toBe(3)
    expect(approvalTabBadgeCount('instances', counts)).toBe(0)
    expect(approvalTabBadgeCount('history', counts)).toBe(0)
  })
})
