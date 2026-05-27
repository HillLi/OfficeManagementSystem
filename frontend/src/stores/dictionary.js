import { defineStore } from 'pinia'
import { ref } from 'vue'
import { api } from '../api'
import { catalogLabel, catalogOptions, dictionaryCacheKey, readCachedCatalog } from '../utils/dictionaries'

export const useDictionaryStore = defineStore('dictionary', () => {
  const catalog = ref(readCachedCatalog())

  const labelOf = (type, code) => catalogLabel(catalog.value, type, code)
  const optionsOf = (type) => catalogOptions(catalog.value, type)

  async function refresh(force = false) {
    if (!force && catalog.value) {
      const version = await api.dictionaryVersion()
      if (version === catalog.value.version) return catalog.value
    }
    catalog.value = await api.dictionaries()
    localStorage.setItem(dictionaryCacheKey, JSON.stringify(catalog.value))
    return catalog.value
  }

  function restoreCached() {
    catalog.value = readCachedCatalog()
  }

  return { catalog, labelOf, optionsOf, refresh, restoreCached }
})
