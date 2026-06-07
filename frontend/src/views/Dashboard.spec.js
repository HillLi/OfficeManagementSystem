import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const currentDir = dirname(fileURLToPath(import.meta.url))
const dashboardSource = readFileSync(resolve(currentDir, 'Dashboard.vue'), 'utf-8')
const routerSource = readFileSync(resolve(currentDir, '../router/index.js'), 'utf-8')

describe('Dashboard announcement links', () => {
  it('opens the selected announcement title in a detail dialog without a new tab', () => {
    expect(dashboardSource).toContain('@click="openAnnouncement(item)"')
    expect(dashboardSource).toContain('v-model="detailVisible"')
    expect(dashboardSource).toContain(':close-on-click-modal="false"')
    expect(dashboardSource).toContain('{{ selectedAnnouncement.content }}')
    expect(dashboardSource).not.toContain('target="_blank"')
    expect(dashboardSource).not.toContain('announcementHref')
  })

  it('does not register a separate announcement detail page route', () => {
    expect(routerSource).not.toContain("name: 'announcement-detail'")
    expect(routerSource).not.toContain("path: '/announcements/:id'")
    expect(routerSource).not.toContain('AnnouncementDetail')
  })

  it('shows readable empty states when dashboard chart datasets are empty', () => {
    expect(dashboardSource).toContain('hasDocumentStatusData')
    expect(dashboardSource).toContain('hasMonthlyBusinessData')
    expect(dashboardSource).toContain('hasTravelBudgetData')
    expect(dashboardSource).toContain('暂无数据')
  })

  it('renders a dashboard schedule calendar without approval tasks', () => {
    expect(dashboardSource).toContain('日程管理')
    expect(dashboardSource).toContain('monthlyScheduleItems')
    expect(dashboardSource).toContain('calendar-days')
    expect(dashboardSource).toContain('schedule-item')
    expect(dashboardSource).toContain('当日暂无会议或活动')
    expect(dashboardSource).not.toContain('flowTasks')
    expect(dashboardSource).not.toContain('待审批任务')
  })
})
