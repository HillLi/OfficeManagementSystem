import { describe, expect, it } from 'vitest'
import { statusTagType, statusText, stepClass, typeText } from './workflowGuide'

describe('workflow guide display helpers', () => {
  it('maps workflow statuses to stable Chinese labels and tag types', () => {
    expect(statusText('current')).toBe('当前节点')
    expect(statusTagType('rejected')).toBe('danger')
    expect(statusText('done')).toBe('已完成')
    expect(statusTagType('waiting')).toBe('info')
  })

  it('keeps unsupported values visible instead of hiding backend data', () => {
    expect(statusText('paused')).toBe('paused')
    expect(statusTagType('paused')).toBe('info')
    expect(typeText('custom')).toBe('custom')
  })

  it('builds step classes from backend status', () => {
    expect(stepClass({ status: 'done' })).toEqual(['flow-step', 'is-done'])
    expect(stepClass({})).toEqual(['flow-step', 'is-waiting'])
  })
})
