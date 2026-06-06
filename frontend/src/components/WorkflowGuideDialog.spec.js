import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const currentDir = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(currentDir, 'WorkflowGuideDialog.vue'), 'utf-8')

describe('Workflow guide dialog time display contract', () => {
  it('formats processing time and due time instead of binding raw ISO values', () => {
    expect(source).toContain('formatDate(row.time)')
    expect(source).toContain('formatDate(row.dueTime)')
    expect(source).not.toContain('prop="time" label="处理时间"')
    expect(source).not.toContain('prop="dueTime" label="截止时间"')
  })
})
