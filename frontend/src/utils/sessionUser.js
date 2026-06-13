// 会话用户信息的读写工具

/** 用户信息在 Storage 中的键名 */
const USER_KEY = 'oms_user'

/** 从 Storage 中读取当前登录用户信息 */
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

/** 将用户信息写入 Storage */
export function writeSessionUser(user, storage = sessionStorage) {
  storage.setItem(USER_KEY, JSON.stringify(user))
}
