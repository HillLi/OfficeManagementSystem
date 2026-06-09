import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const currentDir = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(currentDir, 'Travels.vue'), 'utf-8')

describe('Travels page regression coverage', () => {
  it('keeps travel application fields and submit actions in the dialog', () => {
    expect(source).toContain('差旅申请')
    expect(source).toContain('@click="applicationDialog = true"')
    expect(source).toContain('目的地')
    expect(source).toContain('出差事由')
    expect(source).toContain('出发日期')
    expect(source).toContain('返回日期')
    expect(source).toContain('人员类别')
    expect(source).toContain('出差类型')
    expect(source).toContain('交通工具')
    expect(source).toContain('提交差旅')
  })

  it('keeps reimbursement and workflow actions available from approved rows', () => {
    expect(source).toContain("row.status === 'approved'")
    expect(source).toContain('报销登记')
    expect(source).toContain('openReimburse')
    expect(source).toContain('差旅报销登记')
    expect(source).toContain('实际报销金额')
    expect(source).toContain('票据附件地址')
    expect(source).toContain('超标准说明')
    expect(source).toContain('提交财务复核')
    expect(source).toContain("flowGuideDialog.value?.open('travel', travel.id)")
  })
})
