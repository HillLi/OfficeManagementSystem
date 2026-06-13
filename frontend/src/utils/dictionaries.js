// 数据字典工具函数，提供字典标签查询与缓存读取

/** 字典目录在 localStorage 中的缓存键名 */
export const dictionaryCacheKey = 'oms_dictionary_catalog'

/** 根据字典类型和编码获取对应的显示标签 */
export function catalogLabel(catalog, type, code) {
  const item = (catalog?.dictionaries?.[type] || []).find((entry) => entry.dictCode === code)
  return item?.dictLabel || code || '-'
}

/** 根据字典类型生成下拉选项列表（仅启用项，按排序字段升序） */
export function catalogOptions(catalog, type) {
  return [...(catalog?.dictionaries?.[type] || [])]
    .filter((entry) => entry.enabled)
    .sort((left, right) => (left.sortOrder || 0) - (right.sortOrder || 0))
    .map((entry) => ({ label: entry.dictLabel, value: entry.dictCode }))
}

/** 从 localStorage 中读取缓存的字典目录 */
export function readCachedCatalog(storage = globalThis.localStorage) {
  try {
    const catalog = JSON.parse(storage?.getItem(dictionaryCacheKey) || 'null')
    return catalog?.version && catalog?.dictionaries ? catalog : null
  } catch (error) {
    return null
  }
}
