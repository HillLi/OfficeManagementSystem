import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const currentDir = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(currentDir, 'Announcements.vue'), 'utf-8')
const apiSource = readFileSync(resolve(currentDir, '../api.js'), 'utf-8')

describe('Announcements page department scope', () => {
  it('selects departments by name while keeping id values for persistence', () => {
    expect(source).not.toContain('部门编号')
    expect(source).not.toContain('el-input-number v-model="form.targetDeptId"')
    expect(source).not.toContain('部门 #')
    expect(source).toContain('label="指定部门"')
    expect(source).toContain('v-model="form.targetDeptId"')
    expect(source).toContain('v-for="dept in deptOptions"')
    expect(source).toContain(':label="dept.deptName"')
    expect(source).toContain(':value="dept.id"')
    expect(source).toContain('row.targetDeptName')
  })

  it('falls back when a running backend has not exposed the department options endpoint yet', () => {
    expect(apiSource).toContain("http.get('/auth/dept-options').catch")
    expect(apiSource).toContain("http.get('/admin/depts').catch")
    expect(apiSource).toContain("http.get('/auth/user-options')")
    expect(apiSource).toContain('deptMap.set')
  })
})
