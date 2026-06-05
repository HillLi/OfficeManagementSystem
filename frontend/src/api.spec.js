import { describe, expect, it, vi } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'
import { handleAuthExpired, isAuthExpiredError } from './api'

const currentDir = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(currentDir, 'api.js'), 'utf-8')

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

describe('mail api client methods', () => {
  it('exposes organization tree and mail endpoints', () => {
    expect(source).toContain("orgTree: () => http.get('/org/tree')")
    expect(source).toContain("sendMail: (data) => http.post('/mails', data)")
    expect(source).toContain("mailInbox: (params) => http.get('/mails/inbox', { params })")
    expect(source).toContain("mailSent: (params) => http.get('/mails/sent', { params })")
    expect(source).toContain("mailDetail: (id) => http.get(`/mails/${id}`)")
    expect(source).toContain("markMailRead: (id) => http.post(`/mails/${id}/read`)")
    expect(source).toContain("retryMailEmail: (id) => http.post(`/mails/${id}/retry-email`)")
  })
})
