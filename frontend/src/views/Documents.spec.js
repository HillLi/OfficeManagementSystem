import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const currentDir = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(currentDir, 'Documents.vue'), 'utf-8')

describe('Documents page tabs', () => {
  it('uses tabs to separate document drafting from document records', () => {
    expect(source).toContain('<el-tabs v-model="activeTab">')
    expect(source).toContain('<el-tab-pane label="公文起草" name="draft">')
    expect(source).toContain('<el-tab-pane label="公文列表" name="records">')
    expect(source).toContain("const activeTab = ref('draft')")
  })
})
