import { describe, expect, it, vi } from 'vitest'
import { readSessionUser, writeSessionUser } from './sessionUser'

function storageWith(value) {
  return {
    getItem: vi.fn(() => value),
    setItem: vi.fn(),
    removeItem: vi.fn()
  }
}

describe('session user storage', () => {
  it('returns null and clears corrupted session user data', () => {
    const storage = storageWith('{broken')

    expect(readSessionUser(storage)).toBeNull()
    expect(storage.removeItem).toHaveBeenCalledWith('oms_user')
  })

  it('falls back to the provided user object when session data is missing', () => {
    const fallback = { id: 0, roleKeys: [] }

    expect(readSessionUser(storageWith(null), fallback)).toEqual(fallback)
  })

  it('stores valid users as JSON', () => {
    const storage = storageWith(null)

    writeSessionUser({ id: 2, roleKeys: ['office_user'] }, storage)

    expect(storage.setItem).toHaveBeenCalledWith('oms_user', '{"id":2,"roleKeys":["office_user"]}')
  })
})
