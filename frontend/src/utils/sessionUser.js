const USER_KEY = 'oms_user'

export function readSessionUser(storage = sessionStorage, fallback = null) {
  const raw = storage.getItem(USER_KEY)
  if (!raw) {
    return fallback
  }

  try {
    return JSON.parse(raw)
  } catch (error) {
    storage.removeItem(USER_KEY)
    return fallback
  }
}

export function writeSessionUser(user, storage = sessionStorage) {
  storage.setItem(USER_KEY, JSON.stringify(user))
}
