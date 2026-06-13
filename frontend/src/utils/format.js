// 通用的日期格式化工具

/** 将 ISO 日期字符串格式化为 "YYYY-MM-DD HH:mm" 的可读形式 */
export function formatDate(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '-'
}
