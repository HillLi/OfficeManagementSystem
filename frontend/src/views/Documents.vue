<template>
  <div class="page-grid">
    <div class="panel">
      <h3>公文起草</h3>
      <el-form label-position="top">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="文种">
          <el-select v-model="form.docType"><el-option v-for="item in optionsOf('document_type')" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        </el-form-item>
        <el-form-item label="密级">
          <el-select v-model="form.secrecyLevel"><el-option v-for="item in optionsOf('secrecy_level')" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        </el-form-item>
        <el-form-item label="正文"><el-input v-model="form.content" type="textarea" :rows="6" /></el-form-item>
      </el-form>
      <div class="toolbar">
        <el-button type="primary" @click="save">保存草稿</el-button>
        <el-button :disabled="form.secrecyLevel !== '公开'" @click="draft">AI 起草</el-button>
      </div>
      <p v-if="form.secrecyLevel !== '公开'" class="rule-note">非公开公文禁止调用外部 AI。</p>
    </div>
    <div class="panel">
      <h3>公文列表</h3>
      <el-table :data="rows" border>
        <el-table-column prop="title" label="标题" min-width="185" />
        <el-table-column prop="version" label="版本" width="65" />
        <el-table-column label="密级" width="74"><template #default="{ row }">{{ labelOf('secrecy_level', row.secrecyLevel) }}</template></el-table-column>
        <el-table-column label="流程状态" width="112"><template #default="{ row }">{{ labelOf('business_status', row.status) }}</template></el-table-column>
        <el-table-column label="签收状态" width="112"><template #default="{ row }">{{ labelOf('distribution_status', row.distributionStatus) }}</template></el-table-column>
        <el-table-column label="办理" min-width="310">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" :disabled="!canAi(row)" @click="review(row.id)">AI 审查</el-button>
              <el-button v-if="row.status === 'draft' || row.status === 'rejected'" size="small" type="primary" @click="submitFlow(row.id)">提交</el-button>
              <el-button v-if="row.status === 'approved' && canManage" size="small" type="success" @click="archive(row.id)">归档</el-button>
              <el-button size="small" @click="openAttachment(row)">附件</el-button>
              <el-button v-if="canManage && ['approved', 'archived'].includes(row.status)" size="small" @click="openDistribution(row)">分发/签收</el-button>
              <el-button v-else-if="hasDistribution(row)" size="small" @click="openDistribution(row)">签收记录</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="distributionDialog" title="公文分发与签收" width="820px">
      <el-form v-if="canManage" :inline="true">
        <el-form-item label="接收人">
          <el-select v-model="distributionForm.receiverId" filterable @change="selectReceiver">
            <el-option v-for="user in userOptions" :key="user.id" :label="user.realName" :value="user.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="接收部门"><el-input :model-value="selectedReceiver?.deptName || '-'" disabled /></el-form-item>
        <el-form-item><el-button type="primary" @click="distribute">发起分发</el-button></el-form-item>
      </el-form>
      <el-table :data="distributions" border>
        <el-table-column label="接收人" width="112"><template #default="{ row }">{{ userName(row.receiverId) }}</template></el-table-column>
        <el-table-column label="部门" min-width="126"><template #default="{ row }">{{ userDepartment(row.receiverId) }}</template></el-table-column>
        <el-table-column label="状态" width="112"><template #default="{ row }">{{ labelOf('distribution_status', row.status) }}</template></el-table-column>
        <el-table-column prop="distributedAt" label="分发时间" width="176" />
        <el-table-column prop="receivedAt" label="签收时间" width="176" />
        <el-table-column label="办理" width="150">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button v-if="row.status !== 'received' && row.receiverId === currentUser.id" size="small" type="success" @click="receive(row.id)">签收</el-button>
              <el-button v-if="row.status !== 'received' && canManage" size="small" @click="remind(row.id)">催办</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="attachmentDialog" title="附件管理" width="620px">
      <el-form label-position="top">
        <el-form-item label="文件名"><el-input v-model="attachmentForm.fileName" /></el-form-item>
        <el-form-item label="文件地址"><el-input v-model="attachmentForm.fileUrl" /></el-form-item>
        <el-form-item label="密级">
          <el-select v-model="attachmentForm.secrecyLevel"><el-option v-for="item in optionsOf('secrecy_level')" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="addAttachment">保存附件</el-button>
      <el-table :data="attachments" border style="margin-top: 12px">
        <el-table-column prop="fileName" label="文件名" />
        <el-table-column prop="fileUrl" label="地址" />
        <el-table-column label="密级" width="90"><template #default="{ row }">{{ labelOf('secrecy_level', row.secrecyLevel) }}</template></el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import { useDictionaryStore } from '../stores/dictionary'

const dictionaryStore = useDictionaryStore()
const labelOf = dictionaryStore.labelOf
const optionsOf = dictionaryStore.optionsOf
const currentUser = JSON.parse(sessionStorage.getItem('oms_user') || '{"id":0,"roleKeys":[]}')
const canManage = computed(() => currentUser.roleKeys?.some((role) => ['office_admin', 'admin'].includes(role)))
const rows = ref([])
const userOptions = ref([])
const attachments = ref([])
const distributions = ref([])
const attachmentDialog = ref(false)
const distributionDialog = ref(false)
const currentDocument = ref(null)
const form = reactive({
  title: '关于开展办公管理系统试运行的通知',
  docType: '通知',
  secrecyLevel: '公开',
  content: '请各单位按工作安排开展系统试运行，并及时反馈使用情况。',
  applicantId: currentUser.id || 2
})
const attachmentForm = reactive({ fileName: '', fileUrl: '', secrecyLevel: '公开' })
const distributionForm = reactive({ receiverId: 2, receiverDeptId: 1 })
const selectedReceiver = computed(() => userOptions.value.find((user) => user.id === distributionForm.receiverId))

const canAi = (document) => ['公开', 'public'].includes(document.secrecyLevel)
const hasDistribution = (document) => document.distributionStatus && document.distributionStatus !== 'not_distributed'
const load = async () => { rows.value = await api.documents() }
const userName = (id) => userOptions.value.find((user) => user.id === id)?.realName || `#${id}`
const userDepartment = (id) => userOptions.value.find((user) => user.id === id)?.deptName || '-'
const selectReceiver = () => {
  distributionForm.receiverDeptId = selectedReceiver.value?.deptId || null
}

const save = async () => {
  await api.createDocument(form)
  ElMessage.success('草稿已保存')
  await load()
}
const submitFlow = async (id) => {
  await api.submitDocument(id)
  ElMessage.success('已提交审批')
  await load()
}
const archive = async (id) => {
  await api.archiveDocument(id)
  ElMessage.success('公文已归档')
  await load()
}
const draft = async () => {
  if (form.secrecyLevel !== '公开') return
  form.content = await api.aiDraft({
    docType: form.docType,
    topic: '办公管理系统试运行',
    keyPoints: '明确试运行范围、反馈方式和时间要求。'
  })
}
const review = async (id) => {
  const result = await api.aiReview(id)
  ElMessageBox.alert(result.issues.concat(result.suggestions).join('\n') || '审查通过', 'AI 审查结果')
}
const openDistribution = async (document) => {
  currentDocument.value = document
  distributions.value = await api.documentDistributions(document.id)
  distributionDialog.value = true
}
const distribute = async () => {
  await api.distributeDocument(currentDocument.value.id, distributionForm)
  ElMessage.success('公文已分发')
  distributions.value = await api.documentDistributions(currentDocument.value.id)
  await load()
}
const receive = async (distributionId) => {
  await api.receiveDocument(currentDocument.value.id, distributionId)
  ElMessage.success('签收完成')
  distributions.value = await api.documentDistributions(currentDocument.value.id)
  await load()
}
const remind = async (distributionId) => {
  await api.remindDocument(currentDocument.value.id, distributionId)
  ElMessage.success('已发送催办提醒')
  distributions.value = await api.documentDistributions(currentDocument.value.id)
}
const openAttachment = async (document) => {
  currentDocument.value = document
  attachmentForm.fileName = ''
  attachmentForm.fileUrl = ''
  attachmentForm.secrecyLevel = document.secrecyLevel || '公开'
  attachments.value = await api.attachments({ bizType: 'document', bizId: document.id })
  attachmentDialog.value = true
}
const addAttachment = async () => {
  await api.addAttachment({
    bizType: 'document',
    bizId: currentDocument.value.id,
    fileName: attachmentForm.fileName,
    fileUrl: attachmentForm.fileUrl,
    secrecyLevel: attachmentForm.secrecyLevel
  })
  ElMessage.success('附件已保存')
  attachments.value = await api.attachments({ bizType: 'document', bizId: currentDocument.value.id })
}

onMounted(async () => {
  userOptions.value = await api.userOptions()
  selectReceiver()
  await load()
})
</script>
