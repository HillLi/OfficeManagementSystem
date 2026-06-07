import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const __dirname = dirname(fileURLToPath(import.meta.url))

function viewSource(fileName) {
  return readFileSync(resolve(__dirname, fileName), 'utf8')
}

describe('business pages dialog-first layout', () => {
  const pages = [
    { file: 'Documents.vue', button: '公文起草', dialogModel: 'draftDialog' },
    { file: 'Seals.vue', button: '用印申请', dialogModel: 'applicationDialog' },
    { file: 'Travels.vue', button: '差旅申请', dialogModel: 'applicationDialog' },
    { file: 'Reports.vue', button: '提交请示报告', dialogModel: 'applicationDialog' }
  ]

  it.each(pages)('$file keeps the primary entry in a button-triggered dialog', ({ file, button, dialogModel }) => {
    const source = viewSource(file)

    expect(source).not.toContain('<el-tabs')
    expect(source).not.toContain('<el-tab-pane')
    expect(source).toContain(`@click="${dialogModel} = true"`)
    expect(source).toContain(`v-model="${dialogModel}"`)
    expect(source).toContain(button)
  })

  it('Meetings.vue uses tabs with button-triggered dialog', () => {
    const source = viewSource('Meetings.vue')

    expect(source).toContain('<el-tabs')
    expect(source).toContain('我参与的会议')
    expect(source).toContain('openApplicationDialog')
    expect(source).toContain('v-model="applicationDialog"')
    expect(source).toContain('会议申请')
  })

  it('keeps seal transfer as a manager-only dialog action', () => {
    const source = viewSource('Seals.vue')

    expect(source).toContain('v-if="canManage"')
    expect(source).toContain('@click="transferDialog = true"')
    expect(source).toContain('v-model="transferDialog"')
    expect(source).toContain('移交记录')
  })
})
