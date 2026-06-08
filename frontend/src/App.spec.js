import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const currentDir = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(currentDir, 'App.vue'), 'utf-8')

describe('app shell menu badge layout', () => {
  it('uses an inline badge so approval counts align vertically with menu text', () => {
    expect(source).not.toContain('class="menu-badge"')
    expect(source).toContain('class="menu-badge-count"')
    expect(source).toContain('align-items: center')
  })

  it('reads badge counts from each menu route instead of only approvals', () => {
    expect(source).toContain('actionBadgeStore.menuBadgeFor(item.index)')
    expect(source).not.toContain("item.index === '/approvals' ? actionBadgeStore.approvalTotal : 0")
  })
})
