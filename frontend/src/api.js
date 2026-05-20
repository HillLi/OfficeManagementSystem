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
  approve: (bizType, bizId, data) => http.post(`/approvals/${bizType}/${bizId}`, data)
}
