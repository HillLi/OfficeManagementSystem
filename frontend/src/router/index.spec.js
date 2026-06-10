import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'

const currentDir = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(currentDir, 'index.js'), 'utf-8')

describe('router hardening', () => {
  it('uses safe session user parsing in navigation guards', () => {
    expect(source).toContain("import { readSessionUser } from '../utils/sessionUser'")
    expect(source).not.toContain('JSON.parse(sessionStorage')
  })

  it('defines a catch-all not found route', () => {
    expect(source).toContain('/:pathMatch(.*)*')
    expect(source).toContain('NotFound')
    expect(source).toContain("import('../views/NotFound.vue'), meta: { public: true }")
  })
})
