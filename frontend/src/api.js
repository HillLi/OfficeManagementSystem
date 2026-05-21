import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 10000
})

http.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('oms_token')
  if (token) {
    config.headers.Authorization = 'Bearer ' + token
  }
  return config
})

http.interceptors.response.use((response) => {
  const body = response.data
  if (!body.success) {
    return Promise.reject(new Error(body.message || '请求失败'))
  }
  return body.data
})

export const api = {
  login: (data) => http.post('/auth/login', data),
  dashboard: () => http.get('/dashboard'),
  users: () => http.get('/auth/users'),
  documents: () => http.get('/documents'),
  createDocument: (data) => http.post('/documents', data),
  submitDocument: (id) => http.post(`/documents/${id}/submit`),
  aiDraft: (data) => http.post('/documents/ai-draft', data),
  aiReview: (id) => http.post(`/documents/${id}/ai-review`),
  seals: () => http.get('/seals'),
  sealApps: () => http.get('/seals/applications'),
  createSealApp: (data) => http.post('/seals/applications', data),
  rooms: () => http.get('/meetings/rooms'),
  meetings: () => http.get('/meetings'),
  createMeeting: (data) => http.post('/meetings', data),
  travels: () => http.get('/travels'),
  createTravel: (data) => http.post('/travels', data),
  reports: () => http.get('/reports'),
  createReport: (data) => http.post('/reports', data),
  approvals: (params) => http.get('/approvals', { params }),
  approve: (bizType, bizId, data) => http.post(`/approvals/${bizType}/${bizId}`, data),
  addAttachment: (data) => http.post('/workflow/attachments', data),
  attachments: (params) => http.get('/workflow/attachments', { params }),
  auditLogs: (params) => http.get('/workflow/audit-logs', { params }),
  notifications: (params) => http.get('/workflow/notifications', { params }),
  markNotificationRead: (id) => http.post(`/workflow/notifications/${id}/read`),
  flowInstances: () => http.get('/workflow/instances'),
  flowTasks: (params) => http.get('/workflow/tasks', { params }),
  archiveDocument: (id) => http.post(`/documents/${id}/archive`),
  archiveMeetingMinutes: (id, data) => http.post(`/meetings/${id}/minutes`, data),
  reimburseTravel: (id, data) => http.post(`/travels/${id}/reimburse`, data),
  replyReport: (id, data) => http.post(`/reports/${id}/reply`, data),

  // Admin - User management
  adminUsers: () => http.get('/admin/users'),
  adminUser: (id) => http.get(`/admin/users/${id}`),
  adminCreateUser: (data) => http.post('/admin/users', data),
  adminUpdateUser: (id, data) => http.put(`/admin/users/${id}`, data),
  adminDeleteUser: (id) => http.delete(`/admin/users/${id}`),
  adminRoles: () => http.get('/admin/roles'),

  // Admin - Department management
  adminDepts: () => http.get('/admin/depts'),
  adminCreateDept: (data) => http.post('/admin/depts', data),
  adminUpdateDept: (id, data) => http.put(`/admin/depts/${id}`, data),
  adminDeleteDept: (id) => http.delete(`/admin/depts/${id}`)
}
