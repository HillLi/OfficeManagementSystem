<template>
  <div class="mails-page">
    <div class="panel report-header">
      <h3>邮件中心</h3>
    </div>
    <div class="page-actions">
      <el-button type="primary" @click="openCompose">写邮件</el-button>
      <el-button :loading="loading" @click="refreshAll">刷新</el-button>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="收件箱" name="inbox">
        <el-table :data="inboxRows" border stripe v-loading="loading">
          <el-table-column label="主题" min-width="220">
            <template #default="{ row }">
              <button
                type="button"
                class="mail-subject-link"
                :class="{ unread: row.currentUserRead === false }"
                @click.prevent="openDetail(row)"
              >{{ row.subject }}</button>
            </template>
          </el-table-column>
          <el-table-column prop="senderName" label="发件人" width="130" />
          <el-table-column label="类型" width="86">
            <template #default="{ row }">{{ recipientTypeText(row.currentUserRecipientType) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="86">
            <template #default="{ row }">
              <el-tag size="small" :type="readStateType(row.currentUserRead)" class="state-tag">
                {{ readStateText(row.currentUserRead) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="时间" width="176">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="90" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openDetail(row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="已发送" name="sent">
        <el-table :data="sentRows" border stripe v-loading="loading">
          <el-table-column label="主题" min-width="210">
            <template #default="{ row }">
              <button type="button" class="mail-subject-link" @click.prevent="openDetail(row)">
                {{ row.subject }}
              </button>
            </template>
          </el-table-column>
          <el-table-column label="收件人" min-width="190">
            <template #default="{ row }">{{ recipientSummary(row.recipients) }}</template>
          </el-table-column>
          <el-table-column label="外部投递" min-width="180">
            <template #default="{ row }">
              <div class="delivery-summary">
                <el-tag
                  size="small"
                  :type="deliverySummaryType(row.recipients)"
                  class="state-tag"
                >
                  {{ deliverySummaryText(row.recipients) }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="时间" width="176">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button size="small" @click="openDetail(row)">查看</el-button>
                <el-button size="small" type="warning" :loading="retryingId === row.id" @click="retryEmail(row)">重试邮件</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="composeVisible" title="写邮件" width="720px" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="主题">
          <el-input v-model="form.subject" maxlength="255" show-word-limit />
        </el-form-item>
        <el-form-item label="收件人">
          <OrgUserTreeSelect v-model="form.toUserIds" :tree-data="orgTree" />
        </el-form-item>
        <el-form-item label="抄送">
          <OrgUserTreeSelect v-model="form.ccUserIds" :tree-data="orgTree" />
        </el-form-item>
        <el-form-item label="正文">
          <el-input v-model="form.content" type="textarea" :rows="8" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="composeVisible = false">取消</el-button>
        <el-button type="primary" :loading="sending" @click="send">发送</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="邮件详情" width="760px" :close-on-click-modal="false">
      <article v-if="detail" class="mail-detail">
        <h3>{{ detail.subject }}</h3>
        <div class="detail-meta">
          <span>{{ detail.senderName }}</span>
          <span>{{ formatDate(detail.createdAt) }}</span>
          <el-tag v-if="detail.currentUserRecipientType" size="small">
            {{ recipientTypeText(detail.currentUserRecipientType) }}
          </el-tag>
          <el-tag v-if="showCurrentReadState" size="small" :type="readStateType(detail.currentUserRead)">
            {{ readStateText(detail.currentUserRead) }}
          </el-tag>
        </div>
        <div class="detail-content">{{ detail.content }}</div>

        <el-table :data="detail.recipients || []" border size="small">
          <el-table-column label="人员" min-width="130">
            <template #default="{ row }">
              {{ row.realName || userLabel(row.userId) }}
            </template>
          </el-table-column>
          <el-table-column prop="deptName" label="部门" min-width="130" />
          <el-table-column label="类型" width="82">
            <template #default="{ row }">{{ recipientTypeText(row.recipientType) }}</template>
          </el-table-column>
          <el-table-column v-if="showRecipientStates" label="阅读" width="86">
            <template #default="{ row }">
              <el-tag size="small" :type="readStateType(row.readStatus)" class="state-tag">
                {{ readStateText(row.readStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="showRecipientStates" label="外部投递" width="118">
            <template #default="{ row }">
              <el-tag size="small" :type="emailStatusType(row.emailStatus)" class="state-tag">
                {{ emailStatusText(row.emailStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column v-if="showRecipientStates" prop="emailSentAt" label="投递时间" width="176">
            <template #default="{ row }">{{ formatDate(row.emailSentAt) }}</template>
          </el-table-column>
          <el-table-column v-if="showRecipientStates" label="错误" min-width="150" show-overflow-tooltip>
            <template #default="{ row }">{{ externalErrorText(row.emailError) }}</template>
          </el-table-column>
        </el-table>
      </article>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="detail && showRecipientStates"
          type="warning"
          :loading="retryingId === detail.id"
          @click="retryEmail(detail)"
        >重试邮件</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { readSessionUser } from '../utils/sessionUser'
import OrgUserTreeSelect from '../components/OrgUserTreeSelect.vue'
import { formatDate } from '../utils/format'

const activeTab = ref('inbox')
const loading = ref(false)
const sending = ref(false)
const retryingId = ref(null)
const composeVisible = ref(false)
const detailVisible = ref(false)
const inboxRows = ref([])
const sentRows = ref([])
const orgTree = ref([])
const detail = ref(null)

const form = reactive({
  subject: '',
  content: '',
  toUserIds: [],
  ccUserIds: []
})

const currentUser = readSessionUser(undefined, { id: 0 })
const showRecipientStates = computed(() => detail.value && detail.value.senderId === currentUser.id)
const showCurrentReadState = computed(() =>
  ['to', 'cc'].includes(detail.value?.currentUserRecipientType)
)

async function loadInitial() {
  loading.value = true
  try {
    const [tree, inbox, sent] = await Promise.all([
      api.orgTree(),
      api.mailInbox(),
      api.mailSent()
    ])
    orgTree.value = tree
    inboxRows.value = inbox
    sentRows.value = sent
  } catch (error) {
    ElMessage.error(error.message || '加载邮件失败')
  } finally {
    loading.value = false
  }
}

async function refreshAll() {
  await loadInitial()
}

function openCompose() {
  Object.assign(form, {
    subject: '',
    content: '',
    toUserIds: [],
    ccUserIds: []
  })
  composeVisible.value = true
}

async function send() {
  if (!form.subject.trim()) {
    ElMessage.warning('主题不能为空')
    return
  }
  if (form.toUserIds.length === 0) {
    ElMessage.warning('请选择收件人')
    return
  }
  if (!form.content.trim()) {
    ElMessage.warning('正文不能为空')
    return
  }
  sending.value = true
  try {
    await api.sendMail({
      subject: form.subject.trim(),
      content: form.content.trim(),
      toUserIds: form.toUserIds,
      ccUserIds: form.ccUserIds
    })
    ElMessage.success('邮件已发送')
    composeVisible.value = false
    activeTab.value = 'sent'
    await refreshAll()
  } catch (error) {
    ElMessage.error(error.message || '发送失败')
  } finally {
    sending.value = false
  }
}

async function openDetail(row) {
  try {
    detail.value = await api.mailDetail(row.id)
    detailVisible.value = true
    if (activeTab.value === 'inbox' && detail.value.currentUserRead === false) {
      await api.markMailRead(detail.value.id)
      detail.value = await api.mailDetail(detail.value.id)
      await refreshAll()
    }
  } catch (error) {
    ElMessage.error(error.message || '加载详情失败')
  }
}

async function retryEmail(row) {
  retryingId.value = row.id
  try {
    await api.retryMailEmail(row.id)
    ElMessage.success('外部邮件已重试')
    await refreshAll()
    if (detailVisible.value && detail.value?.id === row.id) {
      detail.value = await api.mailDetail(detail.value.id)
    }
  } catch (error) {
    ElMessage.error(error.message || '重试失败')
  } finally {
    retryingId.value = null
  }
}

function recipientTypeText(type) {
  if (type === 'to') return '收件'
  if (type === 'cc') return '抄送'
  if (type === 'sender') return '发件人'
  if (type === 'admin') return '管理员'
  return '-'
}

function readStateText(read) {
  return read ? '已读' : '未读'
}

function readStateType(read) {
  return read ? 'info' : 'danger'
}

function emailStatusText(status) {
  const map = {
    pending: '待发送',
    skipped: '未启用',
    sent: '已发送',
    failed: '失败'
  }
  return map[status] || '未知'
}

function emailStatusType(status) {
  const map = {
    pending: 'warning',
    skipped: 'info',
    sent: 'success',
    failed: 'danger'
  }
  return map[status] || 'info'
}

function externalErrorText(error) {
  if (!error) return '-'
  const map = {
    'external mail disabled': '外部邮件未启用'
  }
  return map[error] || error
}

function recipientSummary(recipients = []) {
  if (!recipients.length) return '-'
  const names = recipients.map((recipient) => recipient.realName || userLabel(recipient.userId))
  if (names.length <= 3) return names.join('、')
  return `${names.slice(0, 3).join('、')} 等 ${names.length} 人`
}

function deliverySummary(recipients = []) {
  const counts = recipients.reduce((result, recipient) => {
    const status = recipient.emailStatus || 'pending'
    result[status] = (result[status] || 0) + 1
    return result
  }, {})
  return Object.entries(counts).map(([status, count]) => ({ status, count }))
}

function deliverySummaryText(recipients = []) {
  const summary = deliverySummary(recipients)
  if (!summary.length) return '-'
  if (summary.length === 1) return emailStatusText(summary[0].status)
  return summary.map((item) => `${emailStatusText(item.status)} ${item.count}人`).join('、')
}

function deliverySummaryType(recipients = []) {
  const summary = deliverySummary(recipients)
  if (!summary.length) return 'info'
  if (summary.some((item) => item.status === 'failed')) return 'danger'
  if (summary.some((item) => item.status === 'pending')) return 'warning'
  if (summary.every((item) => item.status === 'sent')) return 'success'
  return emailStatusType(summary[0].status)
}

function userLabel(userId) {
  return userId ? `#${userId}` : '-'
}

onMounted(loadInitial)
</script>

<style scoped>
.mails-page {
  display: grid;
  gap: 0;
}

.mail-subject-link {
  display: inline-flex;
  max-width: 100%;
  padding: 0;
  border: 0;
  background: transparent;
  color: #1f5f8b;
  font: inherit;
  line-height: 20px;
  text-align: left;
  cursor: pointer;
}

.mail-subject-link.unread {
  font-weight: 700;
}

.delivery-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-height: 24px;
  align-items: center;
}

.state-tag {
  min-width: 52px;
  justify-content: center;
}

.mail-detail {
  display: grid;
  gap: 14px;
}

.mail-detail h3 {
  margin: 0;
  font-size: 18px;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  color: #667085;
  font-size: 13px;
}

.detail-content {
  min-height: 120px;
  padding: 12px;
  border: 1px solid #e3e8ef;
  border-radius: 6px;
  background: #fafcff;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 720px) {
  .mails-page :deep(.el-dialog) {
    width: calc(100vw - 24px);
  }
}
</style>
