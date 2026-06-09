import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const currentDir = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(currentDir, 'Seals.vue'), 'utf-8')

describe('Seals page regression coverage', () => {
  it('keeps seal application, transfer, and workflow actions reachable', () => {
    expect(source).toContain('用印申请')
    expect(source).toContain('@click="applicationDialog = true"')
    expect(source).toContain('印章移交')
    expect(source).toContain('@click="transferDialog = true"')
    expect(source).toContain('材料管理')
    expect(source).toContain('提交审批')
    expect(source).toContain('登记用印')
    expect(source).toContain('确认归还')
    expect(source).toContain('流程导览')
  })

  it('guards privileged seal operations by role-derived permissions', () => {
    expect(source).toContain("['seal_keeper', 'office_admin', 'admin']")
    expect(source).toContain('const canManage = computed')
    expect(source).toContain('const canViewDeleted = computed')
    expect(source).toContain('v-if="canManage"')
    expect(source).toContain('v-if="canViewDeleted"')
  })

  it('covers take-out seal fields and material upload rules', () => {
    expect(source).toContain('是否外带')
    expect(source).toContain('外带原因')
    expect(source).toContain('使用地点')
    expect(source).toContain('预计归还时间')
    expect(source).toContain('保存草稿并上传材料')
    expect(source).toContain('accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"')
    expect(source).toContain('上传材料')
    expect(source).toContain('显示已删除材料')
    expect(source).toContain('删除原因不能为空')
  })
})
