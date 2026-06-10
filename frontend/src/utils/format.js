export function formatDate(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '-'
}
