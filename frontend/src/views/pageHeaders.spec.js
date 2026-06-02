import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const currentDir = dirname(fileURLToPath(import.meta.url))

const pages = [
  ['Announcements.vue', '通知公告'],
  ['Approvals.vue', '审批任务'],
  ['DictionaryManage.vue', '字典管理'],
  ['Documents.vue', '公文管理'],
  ['Meetings.vue', '会议管理'],
  ['Reports.vue', '请示报告'],
  ['Seals.vue', '用印管理'],
  ['Statistics.vue', '统计报表'],
  ['Travels.vue', '差旅审批'],
  ['UserManage.vue', '用户与部门管理']
]

const pagesWithHeaderActions = [
  ['Announcements.vue', '发布公告'],
  ['DictionaryManage.vue', '新增类型'],
  ['Documents.vue', '公文起草'],
  ['Meetings.vue', '会议申请'],
  ['Reports.vue', '提交请示报告'],
  ['Seals.vue', '用印申请'],
  ['Statistics.vue', '导出 CSV'],
  ['Travels.vue', '差旅申请']
]

describe('business page headers', () => {
  it.each(pages)('%s uses the statistics report title presentation', (fileName, title) => {
    const source = readFileSync(resolve(currentDir, fileName), 'utf-8')

    expect(source).toContain('class="panel report-header"')
    expect(source).toContain(`<h3>${title}</h3>`)
    expect(source).not.toContain('class="page-header"')
    expect(source).not.toContain('class="page-title"')
  })

  it.each(pagesWithHeaderActions)('%s keeps title actions below the title card', (fileName, actionText) => {
    const source = readFileSync(resolve(currentDir, fileName), 'utf-8')
    const headerBlock = source.match(/<div class="panel report-header">[\s\S]*?<\/div>/)?.[0] || ''

    expect(headerBlock).not.toContain('<el-button')
    expect(source).toContain('class="page-actions"')
    expect(source).toContain(actionText)
  })
})
