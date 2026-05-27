import { describe, expect, it } from 'vitest'
import { catalogLabel, catalogOptions, readCachedCatalog } from './dictionaries'

const catalog = {
  version: 'catalog-v1',
  dictionaries: {
    business_status: [
      { dictCode: 'approved', dictLabel: '已通过', sortOrder: 30, enabled: true },
      { dictCode: 'draft', dictLabel: '草稿', sortOrder: 10, enabled: true },
      { dictCode: 'archived', dictLabel: '归档', sortOrder: 40, enabled: false }
    ]
  }
}

describe('dictionary catalog helpers', () => {
  it('resolves labels and falls back to existing codes', () => {
    expect(catalogLabel(catalog, 'business_status', 'draft')).toBe('草稿')
    expect(catalogLabel(catalog, 'business_status', 'legacy')).toBe('legacy')
    expect(catalogLabel(catalog, 'business_status', '')).toBe('-')
  })

  it('returns enabled options in configured order', () => {
    expect(catalogOptions(catalog, 'business_status')).toEqual([
      { label: '草稿', value: 'draft' },
      { label: '已通过', value: 'approved' }
    ])
  })

  it('reads valid local catalog data and discards invalid data', () => {
    const goodStorage = { getItem: () => JSON.stringify(catalog) }
    const brokenStorage = { getItem: () => '{broken' }
    expect(readCachedCatalog(goodStorage)).toEqual(catalog)
    expect(readCachedCatalog(brokenStorage)).toBeNull()
  })
})
