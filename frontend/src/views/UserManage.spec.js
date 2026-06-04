import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const currentDir = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(currentDir, 'UserManage.vue'), 'utf-8')

describe('user management page actions', () => {
  it('keeps add user and add department actions below the title card', () => {
    const headerBlock = source.match(/<div class="panel report-header">[\s\S]*?<\/div>/)?.[0] || ''
    const actionsBlock = source.match(/<div class="page-actions">[\s\S]*?<\/div>/)?.[0] || ''

    expect(headerBlock).not.toContain('<el-button')
    expect(actionsBlock).toContain('新增用户')
    expect(actionsBlock).toContain('新增部门')
    expect(actionsBlock).toContain('v-if="activeTab === \'users\'"')
    expect(actionsBlock).toContain('v-if="activeTab === \'depts\'"')
    expect(source).not.toContain('<div style="margin-bottom:12px">')
  })

  it('captures and submits required user email', () => {
    expect(source).toContain('prop="email"')
    expect(source).toContain('v-model="userForm.email"')
    expect(source).toContain("email: user.email")
    expect(source).toContain("email: f.email")
    expect(source).toContain('!f.email')
  })
})
