import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const currentDir = dirname(fileURLToPath(import.meta.url))
const dashboardSource = readFileSync(resolve(currentDir, 'Dashboard.vue'), 'utf-8')
const routerSource = readFileSync(resolve(currentDir, '../router/index.js'), 'utf-8')

describe('Dashboard announcement links', () => {
  it('opens the selected announcement title in a new detail tab', () => {
    expect(dashboardSource).toContain(':href="announcementHref(item.id)"')
    expect(dashboardSource).toContain('target="_blank"')
    expect(dashboardSource).toContain("name: 'announcement-detail'")
  })

  it('registers a dedicated announcement detail route', () => {
    expect(routerSource).toContain("name: 'announcement-detail'")
    expect(routerSource).toContain("path: '/announcements/:id'")
    expect(routerSource).toContain('AnnouncementDetail')
  })
})
