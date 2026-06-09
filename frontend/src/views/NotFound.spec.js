import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const currentDir = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(currentDir, 'NotFound.vue'), 'utf-8')

describe('NotFound page regression coverage', () => {
  it('offers a clear fallback without clearing the session', () => {
    expect(source).toContain('页面不存在')
    expect(source).toContain('请从左侧菜单进入可访问的功能页面。')
    expect(source).toContain("返回首页")
    expect(source).toContain("$router.push('/dashboard')")
    expect(source).not.toContain('sessionStorage.clear')
    expect(source).not.toContain('logout')
  })
})
