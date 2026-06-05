import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const currentDir = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(currentDir, 'OrgUserTreeSelect.vue'), 'utf-8')

describe('organization user tree selector', () => {
  it('renders a checked Element Plus tree with stable user-only selection', () => {
    expect(source).toContain('<el-tree')
    expect(source).toContain('show-checkbox')
    expect(source).toContain('node-key="id"')
    expect(source).toContain('default-expand-all')
    expect(source).toContain(':check-strictly="true"')
    expect(source).toContain(':props="treeProps"')
    expect(source).toContain('node.type === \'user\'')
    expect(source).toContain('emit(\'update:modelValue\'')
  })

  it('accepts modelValue and treeData props, exposes selected user tags, and syncs checked keys', () => {
    expect(source).toContain('modelValue:')
    expect(source).toContain('treeData:')
    expect(source).toContain('selectedUsers')
    expect(source).toContain('<el-tag')
    expect(source).toContain('@close="removeUser')
    expect(source).toContain('removeUser')
    expect(source).toContain('setCheckedKeys')
    expect(source).toContain('user-${id}')
    expect(source).toContain('watch(')
  })
})
