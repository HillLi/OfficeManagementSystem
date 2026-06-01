import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const currentDir = dirname(fileURLToPath(import.meta.url))
const dashboardSource = readFileSync(resolve(currentDir, 'Dashboard.vue'), 'utf-8')
const announcementsSource = readFileSync(resolve(currentDir, 'Announcements.vue'), 'utf-8')

describe('Dashboard announcement links', () => {
  it('opens the selected announcement content from the latest announcement title', () => {
    expect(dashboardSource).toContain('@click="openAnnouncement(item)"')
    expect(dashboardSource).toContain("path: '/announcements'")
    expect(dashboardSource).toContain('focus: item.id')
  })

  it('lets the announcements page focus and highlight an announcement by route query', () => {
    expect(announcementsSource).toContain("useRoute")
    expect(announcementsSource).toContain(':id="`announcement-${row.id}`"')
    expect(announcementsSource).toContain("route.query.focus")
    expect(announcementsSource).toContain('scrollIntoView')
    expect(announcementsSource).toContain('focusedAnnouncementId')
  })
})
