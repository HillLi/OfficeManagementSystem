export const dictionaryCacheKey = 'oms_dictionary_catalog'

export function catalogLabel(catalog, type, code) {
  const item = (catalog?.dictionaries?.[type] || []).find((entry) => entry.dictCode === code)
  return item?.dictLabel || code || '-'
}

export function catalogOptions(catalog, type) {
  return [...(catalog?.dictionaries?.[type] || [])]
    .filter((entry) => entry.enabled)
    .sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0))
    .map((entry) => ({ label: entry.dictLabel, value: entry.dictCode }))
}

export function readCachedCatalog(storage = globalThis.localStorage) {
  try {
    const catalog = JSON.parse(storage?.getItem(dictionaryCacheKey) || 'null')
    return catalog?.version && catalog?.dictionaries ? catalog : null
  } catch (error) {
    return null
  }
}
