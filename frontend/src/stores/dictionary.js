// 数据字典状态管理，负责加载、缓存和查询字典数据
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '../api'
import { catalogLabel, catalogOptions, dictionaryCacheKey, readCachedCatalog } from '../utils/dictionaries'

export const useDictionaryStore = defineStore('dictionary', () => {
  // 字典目录数据，初始化时从本地缓存读取
  const catalog = ref(readCachedCatalog())

  // 根据字典类型和编码获取对应的显示标签
  const labelOf = (type, code) => catalogLabel(catalog.value, type, code)
  // 根据字典类型获取该类型下的所有选项列表
  const optionsOf = (type) => catalogOptions(catalog.value, type)

  // 刷新字典数据，支持强制刷新和版本比对
  async function refresh(force = false) {
    if (!force && catalog.value) {
      const version = await api.dictionaryVersion()
      if (version === catalog.value.version) return catalog.value
    }
    catalog.value = await api.dictionaries()
    localStorage.setItem(dictionaryCacheKey, JSON.stringify(catalog.value))
    return catalog.value
  }

  // 从本地缓存恢复字典数据
  function restoreCached() {
    catalog.value = readCachedCatalog()
  }

  return { catalog, labelOf, optionsOf, refresh, restoreCached }
})
