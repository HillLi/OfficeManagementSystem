import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const currentDir = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(currentDir, 'Reports.vue'), 'utf-8')

describe('Reports page regression coverage', () => {
  it('keeps report submit fields and dictionary-backed selectors', () => {
    expect(source).toContain('提交请示报告')
    expect(source).toContain('@click="applicationDialog = true"')
    expect(source).toContain('标题')
    expect(source).toContain("optionsOf('report_type')")
    expect(source).toContain("optionsOf('secrecy_level')")
    expect(source).toContain('内容')
    expect(source).toContain('api.createReport')
  })

  it('keeps approved report reply and workflow guide actions', () => {
    expect(source).toContain("row.status === 'approved'")
    expect(source).toContain('批复归档')
    expect(source).toContain("ElMessageBox.prompt('请输入批复意见', '批复归档'")
    expect(source).toContain('api.replyReport')
    expect(source).toContain("flowGuideDialog.value?.open('report', row.id)")
  })
})
