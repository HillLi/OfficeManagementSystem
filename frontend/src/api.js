import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000
})

let authExpiredHandling = false
const AUTH_EXPIRED_MESSAGE = '用户未登录或登录已失效'

export function isAuthExpiredError(error) {
  const status = error?.response?.status
  const message = error?.response?.data?.message || error?.message || ''
  return status === 401 || message.includes(AUTH_EXPIRED_MESSAGE)
}

export function handleAuthExpired({
  sessionStorage = globalThis.window?.sessionStorage || globalThis.sessionStorage,
  location = globalThis.window?.location || globalThis.location,
  notify = (message) => ElMessage.warning(message)
} = {}) {
  sessionStorage?.removeItem('oms_user')
  sessionStorage?.removeItem('oms_token')
  if (authExpiredHandling) {
    return
  }
  authExpiredHandling = true
  notify('用户未登录或登录已失效，请重新登录')
  if (location?.pathname !== '/login') {
    location?.assign?.('/login')
  }
}

http.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('oms_token')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

http.interceptors.response.use((response) => {
  if (response.config.responseType === 'blob') {
    return response.data
  }
  const body = response.data
  if (!body.success) {
    const error = new Error(body.message || '请求失败')
    error.response = response
    if (isAuthExpiredError(error)) {
      handleAuthExpired()
    }
    return Promise.reject(error)
  }
  return body.data
}, (error) => {
  if (isAuthExpiredError(error)) {
    handleAuthExpired()
  }
  if (error?.response?.data?.message) {
    error.message = error.response.data.message
  }
  return Promise.reject(error)
})

export const api = {
  login: (data) => http.post('/auth/login', data),
  logout: () => http.post('/auth/logout'),
  dashboard: () => http.get('/dashboard'),
  announcements: (params) => http.get('/announcements', { params }),
  latestAnnouncements: (params) => http.get('/announcements/latest', { params }),
  createAnnouncement: (data) => http.post('/announcements', data),
  updateAnnouncement: (id, data) => http.put(`/announcements/${id}`, data),
  publishAnnouncement: (id) => http.post(`/announcements/${id}/publish`),
  withdrawAnnouncement: (id) => http.post(`/announcements/${id}/withdraw`),
  statistics: () => http.get('/statistics'),
  exportStatistics: () => http.get('/statistics/export', { responseType: 'blob' }),
  users: () => http.get('/auth/users'),
  userOptions: () => http.get('/auth/user-options'),
  deptOptions: () => http.get('/auth/dept-options').catch((error) => {
    if (error?.response?.status !== 404) {
      return Promise.reject(error)
    }
    return http.get('/admin/depts').catch((adminError) => {
      if (![403, 404].includes(adminError?.response?.status)) {
        return Promise.reject(adminError)
      }
      return http.get('/auth/user-options').then((users) => {
        const deptMap = new Map()
        users.forEach((user) => {
          if (user.deptId && user.deptName) {
            deptMap.set(user.deptId, { id: user.deptId, deptName: user.deptName })
          }
        })
        return Array.from(deptMap.values())
      })
    })
  }),
  dictionaries: () => http.get('/dictionaries'),
  dictionaryVersion: () => http.get('/dictionaries/version'),

  documents: () => http.get('/documents'),
  createDocument: (data) => http.post('/documents', data),
  submitDocument: (id) => http.post(`/documents/${id}/submit`),
  archiveDocument: (id) => http.post(`/documents/${id}/archive`),
  documentDistributions: (id) => http.get(`/documents/${id}/distributions`),
  distributeDocument: (id, data) => http.post(`/documents/${id}/distributions`, data),
  receiveDocument: (id, distributionId) => http.post(`/documents/${id}/distributions/${distributionId}/receipt`),
  remindDocument: (id, distributionId) => http.post(`/documents/${id}/distributions/${distributionId}/remind`),
  aiDraft: (data) => http.post('/documents/ai-draft', data),
  aiReview: (id) => http.post(`/documents/${id}/ai-review`),

  seals: () => http.get('/seals'),
  sealApps: () => http.get('/seals/applications'),
  createSealApp: (data) => http.post('/seals/applications', data),
  submitSealApp: (id) => http.post(`/seals/applications/${id}/submit`),
  markSealUsed: (id, keeperId) => http.post(`/seals/applications/${id}/used`, null, { params: { keeperId } }),
  returnSeal: (id, keeperId) => http.post(`/seals/applications/${id}/returned`, null, { params: { keeperId } }),
  sealTransfers: () => http.get('/seals/transfers'),
  createSealTransfer: (data) => http.post('/seals/transfers', data),

  rooms: () => http.get('/meetings/rooms'),
  meetings: () => http.get('/meetings'),
  createMeeting: (data) => http.post('/meetings', data),
  archiveMeetingMinutes: (id, data) => http.post(`/meetings/${id}/minutes`, data),

  travels: () => http.get('/travels'),
  createTravel: (data) => http.post('/travels', data),
  reimburseTravel: (id, data) => http.post(`/travels/${id}/reimburse`, data),

  reports: () => http.get('/reports'),
  createReport: (data) => http.post('/reports', data),
  replyReport: (id, data) => http.post(`/reports/${id}/reply`, data),

  approvals: (params) => http.get('/approvals', { params }),
  approve: (bizType, bizId, data) => http.post(`/approvals/${bizType}/${bizId}`, data),
  addAttachment: (data) => http.post('/workflow/attachments', data),
  attachments: (params) => http.get('/workflow/attachments', { params }),
  uploadAttachment: (data) => http.post('/workflow/attachments/upload', data),
  downloadAttachment: (id) => http.get(`/workflow/attachments/${id}/download`, { responseType: 'blob' }),
  updateAttachment: (id, data) => http.put(`/workflow/attachments/${id}`, data),
  deleteAttachment: (id, data) => http.delete(`/workflow/attachments/${id}`, { data }),
  auditLogs: (params) => http.get('/workflow/audit-logs', { params }),
  notifications: (params) => http.get('/workflow/notifications', { params }),
  markNotificationRead: (id) => http.post(`/workflow/notifications/${id}/read`),
  flowInstances: () => http.get('/workflow/instances'),
  flowTasks: (params) => http.get('/workflow/tasks', { params }),
  workflowGuide: (params) => http.get('/workflow/guide', { params }),

  adminUsers: () => http.get('/admin/users'),
  adminUser: (id) => http.get(`/admin/users/${id}`),
  adminCreateUser: (data) => http.post('/admin/users', data),
  adminUpdateUser: (id, data) => http.put(`/admin/users/${id}`, data),
  adminDeleteUser: (id) => http.delete(`/admin/users/${id}`),
  adminRoles: () => http.get('/admin/roles'),
  adminDepts: () => http.get('/admin/depts'),
  adminCreateDept: (data) => http.post('/admin/depts', data),
  adminUpdateDept: (id, data) => http.put(`/admin/depts/${id}`, data),
  adminDeleteDept: (id) => http.delete(`/admin/depts/${id}`),
  adminDictionaryTypes: () => http.get('/admin/dictionaries/types'),
  adminDictionaryItems: (type) => http.get(`/admin/dictionaries/types/${encodeURIComponent(type)}/items`),
  adminCreateDictionaryType: (data) => http.post('/admin/dictionaries/types', data),
  adminUpdateDictionaryType: (type, data) => http.put(`/admin/dictionaries/types/${encodeURIComponent(type)}`, data),
  adminCreateDictionaryItem: (type, data) => http.post(`/admin/dictionaries/types/${encodeURIComponent(type)}/items`, data),
  adminUpdateDictionaryItem: (type, code, data) => http.put(`/admin/dictionaries/types/${encodeURIComponent(type)}/items/${encodeURIComponent(code)}`, data)
}
