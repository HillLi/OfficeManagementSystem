import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

const currentDir = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(currentDir, 'Mails.vue'), 'utf-8')

describe('Mails page contract', () => {
  it('renders the mail workbench with inbox, sent and compose surfaces', () => {
    expect(source).toContain('class="panel report-header"')
    expect(source).toContain('<h3>邮件中心</h3>')
    expect(source).toContain('@click="openCompose"')
    expect(source).toContain('@click="refreshAll"')
    expect(source).toContain('<el-tabs v-model="activeTab"')
    expect(source).toContain('label="收件箱"')
    expect(source).toContain('label="已发送"')
    expect(source).toContain('<el-dialog v-model="composeVisible"')
    expect(source).toContain('<el-dialog v-model="detailVisible"')
    expect(source).toContain('OrgUserTreeSelect')
  })

  it('loads organization tree, inbox and sent data through the mail APIs', () => {
    expect(source).toContain('api.orgTree()')
    expect(source).toContain('api.mailInbox()')
    expect(source).toContain('api.mailSent()')
    expect(source).toContain('api.mailDetail(row.id)')
    expect(source).toContain('api.markMailRead(detail.value.id)')
    expect(source).toContain('api.retryMailEmail(row.id)')
    expect(source).toContain('onMounted(loadInitial)')
  })

  it('sends compose payload with to and cc user ids after validation', () => {
    expect(source).toContain('toUserIds: []')
    expect(source).toContain('ccUserIds: []')
    expect(source).toContain('!form.subject.trim()')
    expect(source).toContain('!form.content.trim()')
    expect(source).toContain('form.toUserIds.length === 0')
    expect(source).toContain('api.sendMail({')
    expect(source).toContain('toUserIds: form.toUserIds')
    expect(source).toContain('ccUserIds: form.ccUserIds')
  })

  it('shows read state and external delivery status in lists and details', () => {
    expect(source).toContain('currentUserRecipientType')
    expect(source).toContain('currentUserRead')
    expect(source).toContain('readStateText')
    expect(source).toContain('readStateType')
    expect(source).toContain('showCurrentReadState')
    expect(source).toContain("if (type === 'sender') return '发件人'")
    expect(source).toContain('deliverySummary')
    expect(source).toContain('emailStatusText')
    expect(source).toContain('emailStatusType')
    expect(source).toContain('recipientSummary')
    expect(source).toContain('showRecipientStates')
  })

  it('marks unread inbox details as read and refreshes open retry results', () => {
    expect(source).toContain('if (activeTab.value === \'inbox\' && detail.value.currentUserRead === false)')
    expect(source).toContain('await api.markMailRead(detail.value.id)')
    expect(source).toContain('detail.value = await api.mailDetail(detail.value.id)')
    expect(source).toContain('if (detailVisible.value && detail.value?.id === row.id)')
  })

  it('localizes external mail delivery errors in the detail table', () => {
    expect(source).toContain('externalErrorText(row.emailError)')
    expect(source).toContain("'external mail disabled': '外部邮件未启用'")
    expect(source).not.toContain('prop="emailError" label="错误"')
  })

  it('summarizes sent-list external delivery without ambiguous bare counts', () => {
    expect(source).toContain('deliverySummaryText(row.recipients)')
    expect(source).not.toContain('emailStatusText(item.status) }} {{ item.count }}')
  })
})
