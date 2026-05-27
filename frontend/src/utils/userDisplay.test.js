import { describe, expect, it } from 'vitest'
import { displayUserName, originatorIdOf, originatorNameOf } from './userDisplay'

const users = [
  { id: 2, realName: '张三' },
  { id: 3, realName: '李四' }
]

const instances = [
  { id: 10, bizType: 'document', bizId: 101, starterId: 2 },
  { id: 11, bizType: 'meeting', bizId: 102, starterId: 3 }
]

describe('user display helpers', () => {
  it('displays a known user name and falls back to an id label', () => {
    expect(displayUserName(users, 2)).toBe('张三')
    expect(displayUserName(users, 99)).toBe('#99')
    expect(displayUserName(users, null)).toBe('-')
  })

  it('resolves the originator id from approval business rows', () => {
    expect(originatorIdOf({ applicantId: 2 })).toBe(2)
    expect(originatorIdOf({ organizerId: 3 })).toBe(3)
    expect(originatorIdOf({ starterId: 2 })).toBe(2)
  })

  it('resolves approval rows through matching workflow instances', () => {
    expect(originatorNameOf({ bizType: 'document', bizId: 101 }, users, instances)).toBe('张三')
    expect(originatorNameOf({ instanceId: 11 }, users, instances)).toBe('李四')
    expect(originatorNameOf({ bizType: 'seal', bizId: 999 }, users, instances)).toBe('-')
  })
})
