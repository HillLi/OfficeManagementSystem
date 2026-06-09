import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const currentDir = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(currentDir, 'Statistics.vue'), 'utf-8')

describe('Statistics page regression coverage', () => {
  it('keeps all summary metrics visible', () => {
    expect(source).toContain('公文办理')
    expect(source).toContain('待办公文')
    expect(source).toContain('用印申请')
    expect(source).toContain('会议申请')
    expect(source).toContain('大型活动')
    expect(source).toContain('差旅申请')
    expect(source).toContain('请示报告')
    expect(source).toContain('差旅预算总额')
  })

  it('loads statistics and exports csv through the api layer', () => {
    expect(source).toContain('await api.statistics()')
    expect(source).toContain('api.exportStatistics')
    expect(source).toContain('statistics.csv')
    expect(source).toContain('统计报表已导出')
    expect(source).toContain('URL.revokeObjectURL')
  })

  it('renders dictionary labels and monthly business rows', () => {
    expect(source).toContain("labelOf('business_status', status)")
    expect(source).toContain('documentStatusDistribution')
    expect(source).toContain('monthlyBusinessCounts')
    expect(source).toContain('documentStatusRows')
    expect(source).toContain('monthlyRows')
  })
})
