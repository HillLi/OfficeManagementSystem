import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const currentDir = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(currentDir, 'Login.vue'), 'utf-8')

describe('login form security defaults', () => {
  it('does not prefill development credentials', () => {
    expect(source).toContain("const form = reactive({ username: '', password: '' })")
    expect(source).not.toContain("username: 'user'")
    expect(source).not.toContain("password: '123456'")
  })

  it('allows users to toggle password visibility with the Element Plus control', () => {
    expect(source).toContain('show-password')
  })
})
