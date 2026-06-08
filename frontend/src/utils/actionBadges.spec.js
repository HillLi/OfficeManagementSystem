import { describe, expect, it } from 'vitest'
import {
  approvalBadgeCount,
  approvalTabBadgeCount,
  actionableNotificationCount,
  businessActionCounts,
  pendingTaskCount
} from './actionBadges'

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

  it('groups pending approval tasks by business menu', () => {
    const counts = businessActionCounts({
      tasks: [
        { bizType: 'travel', status: 'pending' },
        { bizType: 'travel', status: 'completed' },
        { bizType: 'seal', status: 'pending' },
        { bizType: 'report', status: 'pending' }
      ]
    })

    expect(counts['/travels']).toBe(1)
    expect(counts['/seals']).toBe(1)
    expect(counts['/reports']).toBe(1)
  })

  it('counts unread mail and non-approval business actions for the current role', () => {
    const counts = businessActionCounts({
      currentUser: { id: 2, roleKeys: ['office_user', 'seal_keeper'] },
      tasks: [{ bizType: 'meeting', status: 'pending' }],
      mailInbox: [
        { id: 1, currentUserRead: false },
        { id: 2, currentUserRead: true }
      ],
      travels: [
        { applicantId: 2, status: 'approved', reimbursementSubmitted: false },
        { applicantId: 2, status: 'approved', reimbursementSubmitted: true }
      ],
      reports: [{ status: 'approved' }],
      sealApplications: [
        { status: 'approved' },
        { status: 'used' },
        { status: 'returned' }
      ],
      meetings: [
        { status: 'approved', recorderId: 2 },
        { status: 'minutes_confirmed', organizerId: 2 },
        { status: 'archived', recorderId: 2 }
      ],
      participatedMeetings: [
        { status: 'minutes_pending', minutesConfirmed: false },
        { status: 'minutes_pending', minutesConfirmed: true }
      ],
      documentDistributions: [
        { receiverId: 2, status: 'distributed' },
        { receiverId: 2, status: 'received' }
      ]
    })

    expect(counts['/mails']).toBe(1)
    expect(counts['/travels']).toBe(1)
    expect(counts['/reports']).toBe(0)
    expect(counts['/seals']).toBe(2)
    expect(counts['/meetings']).toBe(4)
    expect(counts['/documents']).toBe(1)
  })

  it('counts office-only report replies and document archive actions', () => {
    const counts = businessActionCounts({
      currentUser: { id: 5, roleKeys: ['office_admin'] },
      reports: [{ status: 'approved' }],
      documents: [
        { status: 'approved' },
        { status: 'archived' }
      ]
    })

    expect(counts['/reports']).toBe(1)
    expect(counts['/documents']).toBe(1)
  })
})
