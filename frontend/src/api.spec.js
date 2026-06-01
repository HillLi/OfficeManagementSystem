import { describe, expect, it, vi } from 'vitest'
import { handleAuthExpired, isAuthExpiredError } from './api'

describe('api authentication expiry handling', () => {
  it('recognizes expired login responses from backend', () => {
    expect(isAuthExpiredError({ response: { status: 401, data: { message: '用户未登录或登录已失效' } } })).toBe(true)
    expect(isAuthExpiredError({ response: { status: 403, data: { message: '无管理员权限' } } })).toBe(false)
  })

  it('clears local login state, notifies once, and redirects to login', () => {
    const sessionStorage = {
      removeItem: vi.fn()
    }
    const location = {
      pathname: '/announcements',
      assign: vi.fn()
    }
    const notify = vi.fn()

    handleAuthExpired({ sessionStorage, location, notify })
    handleAuthExpired({ sessionStorage, location, notify })

    expect(sessionStorage.removeItem).toHaveBeenCalledWith('oms_user')
    expect(sessionStorage.removeItem).toHaveBeenCalledWith('oms_token')
    expect(notify).toHaveBeenCalledTimes(1)
    expect(notify).toHaveBeenCalledWith('用户未登录或登录已失效，请重新登录')
    expect(location.assign).toHaveBeenCalledTimes(1)
    expect(location.assign).toHaveBeenCalledWith('/login')
  })
})
