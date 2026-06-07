import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { resolve, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(__dirname, 'Meetings.vue'), 'utf-8')

describe('Meetings.vue', () => {
  it('has participant selection in application form', () => {
    expect(source).toContain('OrgUserTreeSelect')
    expect(source).toContain('form.participants')
  })

  it('has recorder select', () => {
    expect(source).toContain('记录员')
    expect(source).toContain('form.recorderId')
  })

  it('has participated meetings tab', () => {
    expect(source).toContain('我参与的会议')
    expect(source).toContain('participatedMeetings')
  })

  it('has confirm minutes button', () => {
    expect(source).toContain('confirmMinutes')
    expect(source).toContain('确认纪要')
  })

  it('has publish and archive buttons', () => {
    expect(source).toContain('publishMeeting')
    expect(source).toContain('archiveMeeting')
    expect(source).toContain('发布为公告')
    expect(source).toContain('直接归档')
  })

  it('has confirm progress button', () => {
    expect(source).toContain('确认进度')
    expect(source).toContain('showConfirmProgress')
  })

  it('has minutes_pending and minutes_confirmed status display', () => {
    expect(source).toContain('minutes_pending')
    expect(source).toContain('minutes_confirmed')
  })

  it('auto-calculates expectedCount from participants', () => {
    expect(source).toContain('data.expectedCount = form.participants.length')
  })

  it('has loading state', () => {
    expect(source).toContain('v-loading')
  })

  it('has empty state', () => {
    expect(source).toContain('el-empty')
  })
})
