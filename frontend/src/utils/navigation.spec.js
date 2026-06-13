import { describe, expect, it } from 'vitest'
import { canAccessPath, visibleMenuItems } from './navigation'

function pathsFor(...roles) {
  return visibleMenuItems(roles).map((item) => item.index)
}

describe('role based navigation', () => {
  it('shows business entry menus to an ordinary office user only', () => {
    expect(pathsFor('office_user')).toEqual([
      '/dashboard',
      '/documents',
      '/seals',
      '/meetings',
      '/mails',
      '/travels',
      '/reports',
      '/announcements',
      '/statistics'
    ])
  })

  it('shows approval and business menus for business managers', () => {
    expect(pathsFor('dept_head')).toEqual([
      '/dashboard',
      '/documents',
      '/seals',
      '/meetings',
      '/mails',
      '/travels',
      '/reports',
      '/approvals',
      '/announcements',
      '/statistics'
    ])
    expect(pathsFor('school_leader')).toEqual([
      '/dashboard',
      '/documents',
      '/seals',
      '/meetings',
      '/mails',
      '/travels',
      '/reports',
      '/approvals',
      '/announcements',
      '/statistics'
    ])
    expect(pathsFor('office_admin')).toEqual([
      '/dashboard',
      '/documents',
      '/seals',
      '/meetings',
      '/mails',
      '/reports',
      '/approvals',
      '/announcements',
      '/statistics'
    ])
  })

  it('shows responsibility menus for specialist roles', () => {
    expect(pathsFor('finance_staff')).toEqual(['/dashboard', '/mails', '/travels', '/approvals', '/announcements', '/statistics'])
    expect(pathsFor('security_staff')).toEqual(['/dashboard', '/meetings', '/mails', '/approvals', '/announcements', '/statistics'])
    expect(pathsFor('seal_keeper')).toEqual(['/dashboard', '/seals', '/mails', '/announcements', '/statistics'])
  })

  it('limits an administrator to dashboard statistics and system administration', () => {
    expect(pathsFor('admin')).toEqual([
      '/dashboard',
      '/mails',
      '/announcements',
      '/statistics',
      '/admin/users',
      '/admin/dictionaries',
      '/admin/workflow'
    ])
  })

  it('uses the same policy for direct page navigation', () => {
    expect(canAccessPath('/travels', ['finance_staff'])).toBe(true)
    expect(canAccessPath('/reports', ['finance_staff'])).toBe(false)
    expect(canAccessPath('/approvals', ['finance_staff'])).toBe(true)
    expect(canAccessPath('/announcements', ['office_user'])).toBe(true)
    expect(canAccessPath('/mails', ['office_user'])).toBe(true)
    expect(canAccessPath('/mails', ['admin'])).toBe(true)
    expect(canAccessPath('/admin/dictionaries', ['office_user'])).toBe(false)
    expect(canAccessPath('/admin/dictionaries', ['admin'])).toBe(true)
    expect(canAccessPath('/admin/workflow', ['office_user'])).toBe(false)
    expect(canAccessPath('/admin/workflow', ['admin'])).toBe(true)
  })
})
