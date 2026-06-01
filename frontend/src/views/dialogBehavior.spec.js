import { describe, expect, it } from 'vitest'
import { readdirSync, readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, extname, join, relative } from 'node:path'

const srcDir = dirname(dirname(fileURLToPath(import.meta.url)))

function vueFiles(dir) {
  return readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const path = join(dir, entry.name)
    if (entry.isDirectory()) return vueFiles(path)
    return extname(entry.name) === '.vue' ? [path] : []
  })
}

describe('dialog behavior', () => {
  it('requires every dialog to stay open when the modal backdrop is clicked', () => {
    const dialogs = vueFiles(srcDir).flatMap((file) => {
      const source = readFileSync(file, 'utf8')
      return [...source.matchAll(/<el-dialog\b[^>]*>/g)].map((match) => ({
        file: relative(srcDir, file),
        tag: match[0]
      }))
    })

    expect(dialogs.length).toBeGreaterThan(0)
    expect(dialogs).toEqual(
      dialogs.map((dialog) => ({
        ...dialog,
        tag: expect.stringContaining(':close-on-click-modal="false"')
      }))
    )
  })
})
