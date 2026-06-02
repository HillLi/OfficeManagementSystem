import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const currentDir = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(currentDir, 'DictionaryManage.vue'), 'utf-8')

describe('Dictionary management page header', () => {
  it('matches the statistics report header style without helper copy', () => {
    expect(source).toContain('class="panel report-header"')
    expect(source).not.toContain('class="dictionary-header"')
    expect(source).not.toContain('<p>')
    expect(source).not.toContain('</p>')
  })

  it('shows dictionary types and items as separate stacked management sections', () => {
    expect(source).not.toContain('<el-tabs')
    expect(source).not.toContain('<el-tab-pane')
    expect(source).toContain('class="dictionary-stack"')
    expect(source).toContain('class="panel dictionary-section"')
    expect(source).toContain('class="section-title"')
    expect(source).toContain('字典类型')
    expect(source).toContain('字典项目')
    expect(source).toContain('@row-click="selectTypeRow"')
    expect(source).toContain(':row-class-name="typeRowClassName"')
    expect(source).toContain('selectedTypeTitle')
    expect(source).toContain('请选择上方字典类型')
  })

  it('paginates dictionary types at ten rows per page', () => {
    expect(source).toContain(':data="paginatedTypes"')
    expect(source).toContain('class="type-pagination"')
    expect(source).toContain(':page-size="typePageSize"')
    expect(source).toContain(':current-page="typePage"')
    expect(source).toContain('@current-change="changeTypePage"')
    expect(source).toContain('const typePageSize = 10')
    expect(source).toContain('const paginatedTypes = computed')
  })

  it('shows type editing as a normal button and includes type code in item title', () => {
    expect(source).not.toContain('<el-button link @click.stop="openType(row)">编辑类型</el-button>')
    expect(source).toContain('<el-button size="small" @click.stop="openType(row)">编辑类型</el-button>')
    expect(source).toContain('selectedTypeTitle')
    expect(source).toContain('selectedDictionaryType.value?.dictType')
  })

  it('shows dictionary item editing as a normal button', () => {
    expect(source).not.toContain('<el-button link @click="openItem(row)">编辑</el-button>')
    expect(source).toContain('<el-button size="small" @click="openItem(row)">编辑</el-button>')
  })
})
