import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const currentDir = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(currentDir, 'Approvals.vue'), 'utf-8')

describe('Approvals page time display contract', () => {
  it('formats task due time and approval record time for table display', () => {
    expect(source).toContain('formatDate(row.dueTime)')
    expect(source).toContain('formatDate(row.createdAt)')
    expect(source).not.toContain('prop="dueTime" label="截止时间"')
    expect(source).not.toContain('prop="createdAt" label="时间"')
  })
})
