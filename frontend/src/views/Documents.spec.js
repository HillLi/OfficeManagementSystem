import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const currentDir = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(currentDir, 'Documents.vue'), 'utf-8')

describe('Documents page layout', () => {
  it('keeps document records visible and opens drafting from a dialog button', () => {
    expect(source).not.toContain('<el-tabs')
    expect(source).toContain('@click="draftDialog = true"')
    expect(source).toContain('<el-table :data="rows" border>')
    expect(source).toContain('<el-dialog v-model="draftDialog" title="公文起草"')
    expect(source).toContain('const draftDialog = ref(false)')
  })
})
