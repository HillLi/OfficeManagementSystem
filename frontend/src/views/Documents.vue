<template>
  <div class="document-page">
    <div class="panel report-header">
      <h3>公文管理</h3>
    </div>
    <div class="page-actions">
      <el-button type="primary" @click="draftDialog = true">公文起草</el-button>
    </div>

    <el-table :data="rows" border v-loading="loading">
      <el-table-column prop="title" label="标题" min-width="185" />
      <el-table-column prop="version" label="版本" width="65" />
      <el-table-column label="密级" width="74"><template #default="{ row }">{{ labelOf('secrecy_level', row.secrecyLevel) }}</template></el-table-column>
      <el-table-column label="流程状态" width="112"><template #default="{ row }">{{ labelOf('business_status', row.status) }}</template></el-table-column>
      <el-table-column label="签收状态" width="112"><template #default="{ row }">{{ labelOf('distribution_status', row.distributionStatus) }}</template></el-table-column>
      <el-table-column label="办理" min-width="310">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button size="small" :disabled="!canAi(row)" @click="review(row.id)">AI 审查</el-button>
            <el-button v-if="row.status === 'draft' || row.status === 'rejected'" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'draft' || row.status === 'rejected'" size="small" type="primary" @click="submitFlow(row.id)">提交</el-button>
            <el-button v-if="row.status === 'approved' && canManage" size="small" type="success" @click="archive(row.id)">归档</el-button>
            <el-button size="small" @click="openAttachment(row)">附件</el-button>
            <el-button size="small" @click="openFlowGuide(row)">流程导览</el-button>
            <el-button v-if="canManage && ['approved', 'archived'].includes(row.status)" size="small" @click="openDistribution(row)">分发/签收</el-button>
            <el-button v-else-if="hasDistribution(row)" size="small" @click="openDistribution(row)">签收记录</el-button>
          </div>
        </template>
      </el-table-column>
      <template #empty><el-empty description="暂无公文数据" /></template>
    </el-table>

    <el-dialog v-model="draftDialog" title="公文起草" width="620px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="文种" prop="docType">
          <el-select v-model="form.docType"><el-option v-for="item in optionsOf('document_type')" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        </el-form-item>
        <el-form-item label="密级">
          <el-select v-model="form.secrecyLevel"><el-option v-for="item in optionsOf('secrecy_level')" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        </el-form-item>
        <el-form-item label="正文" prop="content"><el-input v-model="form.content" type="textarea" :rows="6" /></el-form-item>
      </el-form>
      <p v-if="form.secrecyLevel !== '公开'" class="rule-note">非公开公文禁止调用外部 AI。</p>
      <template #footer>
        <el-button @click="draftDialog = false">取消</el-button>
        <el-button :disabled="form.secrecyLevel !== '公开'" @click="draft">AI 起草</el-button>
        <el-button type="primary" @click="save">保存草稿</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialog" title="编辑公文" width="620px" :close-on-click-modal="false">
      <el-form ref="editFormRef" :model="editForm" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title"><el-input v-model="editForm.title" /></el-form-item>
        <el-form-item label="文种" prop="docType">
          <el-select v-model="editForm.docType"><el-option v-for="item in optionsOf('document_type')" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        </el-form-item>
        <el-form-item label="密级">
          <el-select v-model="editForm.secrecyLevel"><el-option v-for="item in optionsOf('secrecy_level')" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        </el-form-item>
        <el-form-item label="正文" prop="content"><el-input v-model="editForm.content" type="textarea" :rows="6" /></el-form-item>
      </el-form>
      <p v-if="editForm.secrecyLevel !== '公开'" class="rule-note">非公开公文禁止调用外部 AI。</p>
      <template #footer>
        <el-button @click="editDialog = false">取消</el-button>
        <el-button :disabled="editForm.secrecyLevel !== '公开'" @click="editAiDraft">AI 起草</el-button>
        <el-button type="primary" @click="saveEdit">保存修改</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="distributionDialog" title="公文分发与签收" width="820px" :close-on-click-modal="false">
      <el-form v-if="canManage" ref="distributionFormRef" :model="distributionForm" :rules="distributionRules" :inline="true">
        <el-form-item label="接收人" prop="receiverId">
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
        <template #empty><el-empty description="暂无公文数据" /></template>
      </el-table>
    </el-dialog>

    <el-dialog v-model="attachmentDialog" title="附件管理" width="620px" :close-on-click-modal="false">
      <el-form ref="attachmentFormRef" :model="attachmentForm" :rules="attachmentRules" label-position="top">
        <el-form-item label="文件名" prop="fileName"><el-input v-model="attachmentForm.fileName" /></el-form-item>
        <el-form-item label="文件地址" prop="fileUrl"><el-input v-model="attachmentForm.fileUrl" /></el-form-item>
        <el-form-item label="密级">
          <el-select v-model="attachmentForm.secrecyLevel"><el-option v-for="item in optionsOf('secrecy_level')" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="addAttachment">保存附件</el-button>
      <el-table :data="attachments" border style="margin-top: 12px">
        <el-table-column prop="fileName" label="文件名" />
        <el-table-column prop="fileUrl" label="地址" />
        <el-table-column label="密级" width="90"><template #default="{ row }">{{ labelOf('secrecy_level', row.secrecyLevel) }}</template></el-table-column>
        <template #empty><el-empty description="暂无公文数据" /></template>
      </el-table>
    </el-dialog>
    <WorkflowGuideDialog ref="flowGuideDialog" />
  </div>
</template>

<script setup>
// 公文管理页面：提供公文起草、审批、归档、分发签收、AI起草/审查等全生命周期管理功能
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import { useDictionaryStore } from '../stores/dictionary'
import { readSessionUser } from '../utils/sessionUser'
import WorkflowGuideDialog from '../components/WorkflowGuideDialog.vue'

const dictionaryStore = useDictionaryStore()
const labelOf = dictionaryStore.labelOf
const optionsOf = dictionaryStore.optionsOf
/** 当前登录用户信息 */
const currentUser = readSessionUser(undefined, { id: 0, roleKeys: [] })
/** 是否具有公文管理权限（办公室管理员或系统管理员） */
const canManage = computed(() => currentUser.roleKeys?.some((role) => ['office_admin', 'admin'].includes(role)))
const rows = ref([])
const loading = ref(false)
const userOptions = ref([])
const attachments = ref([])
const distributions = ref([])
const draftDialog = ref(false)
const editDialog = ref(false)
const attachmentDialog = ref(false)
const distributionDialog = ref(false)
const currentDocument = ref(null)
const flowGuideDialog = ref(null)
const formRef = ref(null)
const distributionFormRef = ref(null)
const attachmentFormRef = ref(null)
const form = reactive({
  title: '',
  docType: '',
  secrecyLevel: '公开',
  content: '',
  applicantId: currentUser.id
})
const attachmentForm = reactive({ fileName: '', fileUrl: '', secrecyLevel: '公开' })
const editForm = reactive({ title: '', docType: '', secrecyLevel: '公开', urgency: '普通', knowledgeScope: '全校', content: '' })
const editFormRef = ref(null)
const editingDoc = ref(null)
const distributionForm = reactive({ receiverId: 2, receiverDeptId: 1 })
const selectedReceiver = computed(() => userOptions.value.find((user) => user.id === distributionForm.receiverId))
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  docType: [{ required: true, message: '请选择文种', trigger: 'change' }],
  content: [{ required: true, message: '请输入正文', trigger: 'blur' }]
}
const distributionRules = {
  receiverId: [{ required: true, message: '请选择接收人', trigger: 'change' }]
}
const attachmentRules = {
  fileName: [{ required: true, message: '请输入文件名', trigger: 'blur' }],
  fileUrl: [{ required: true, message: '请输入文件地址', trigger: 'blur' }]
}

/** 判断公文是否允许使用AI（仅公开密级可用） */
const canAi = (document) => ['公开', 'public'].includes(document.secrecyLevel)
/** 判断公文是否已有分发记录 */
const hasDistribution = (document) => document.distributionStatus && document.distributionStatus !== 'not_distributed'
/** 加载公文列表数据 */
const load = async () => {
  loading.value = true
  try {
    rows.value = await api.documents()
  } finally {
    loading.value = false
  }
}
/** 根据用户ID获取用户姓名 */
const userName = (id) => userOptions.value.find((user) => user.id === id)?.realName || `#${id}`
/** 根据用户ID获取所属部门名称 */
const userDepartment = (id) => userOptions.value.find((user) => user.id === id)?.deptName || '-'
/** 选择接收人时同步其部门ID */
const selectReceiver = () => {
  distributionForm.receiverDeptId = selectedReceiver.value?.deptId || null
}

/** 保存公文草稿 */
const save = async () => {
  try {
    await formRef.value.validate()
    await api.createDocument(form)
    ElMessage.success('草稿已保存')
    draftDialog.value = false
    await load()
  } catch (e) {
    if (e.message !== 'validation failed') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}
/** 打开编辑公文弹窗，填充现有数据 */
const openEdit = (row) => {
  editingDoc.value = row
  editForm.title = row.title
  editForm.docType = row.docType
  editForm.secrecyLevel = row.secrecyLevel || '公开'
  editForm.urgency = row.urgency || '普通'
  editForm.knowledgeScope = row.knowledgeScope || '全校'
  editForm.content = row.content || ''
  editDialog.value = true
}
/** 保存编辑后的公文内容 */
const saveEdit = async () => {
  try {
    await editFormRef.value.validate()
    await api.updateDocument(editingDoc.value.id, { ...editForm, applicantId: editingDoc.value.applicantId })
    ElMessage.success('公文已修改')
    editDialog.value = false
    await load()
  } catch (e) {
    if (e.message !== 'validation failed') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}
/** 调用AI起草公文正文（编辑模式） */
const editAiDraft = async () => {
  if (editForm.secrecyLevel !== '公开') return
  try {
    editForm.content = await api.aiDraft({
      docType: editForm.docType,
      topic: editForm.title,
      keyPoints: '明确试运行范围、反馈方式和时间要求。'
    })
    ElMessage.success('AI 起草完成')
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}
/** 提交公文进入审批流程 */
const submitFlow = async (id) => {
  try {
    await api.submitDocument(id)
    ElMessage.success('已提交审批')
    await load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}
/** 归档已审批通过的公文 */
const archive = async (id) => {
  try {
    await api.archiveDocument(id)
    ElMessage.success('公文已归档')
    await load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}
/** 调用AI起草公文正文（新建模式） */
const draft = async () => {
  if (form.secrecyLevel !== '公开') return
  try {
    form.content = await api.aiDraft({
      docType: form.docType,
      topic: '办公管理系统试运行',
      keyPoints: '明确试运行范围、反馈方式和时间要求。'
    })
    ElMessage.success('AI 起草完成')
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}
/** AI审查公文，返回问题和建议 */
const review = async (id) => {
  try {
    const result = await api.aiReview(id)
    ElMessageBox.alert(result.issues.concat(result.suggestions).join('\n') || '审查通过', 'AI 审查结果')
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}
/** 打开公文分发与签收弹窗 */
const openDistribution = async (document) => {
  try {
    currentDocument.value = document
    distributions.value = await api.documentDistributions(document.id)
    distributionDialog.value = true
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}
/** 发起公文分发 */
const distribute = async () => {
  try {
    await distributionFormRef.value.validate()
    await api.distributeDocument(currentDocument.value.id, distributionForm)
    ElMessage.success('公文已分发')
    distributions.value = await api.documentDistributions(currentDocument.value.id)
    await load()
  } catch (e) {
    if (e.message !== 'validation failed') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}
/** 签收公文 */
const receive = async (distributionId) => {
  try {
    await api.receiveDocument(currentDocument.value.id, distributionId)
    ElMessage.success('签收完成')
    distributions.value = await api.documentDistributions(currentDocument.value.id)
    await load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}
/** 对未签收的公文发送催办提醒 */
const remind = async (distributionId) => {
  try {
    await api.remindDocument(currentDocument.value.id, distributionId)
    ElMessage.success('已发送催办提醒')
    distributions.value = await api.documentDistributions(currentDocument.value.id)
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}
/** 打开附件管理弹窗 */
const openAttachment = async (document) => {
  try {
    currentDocument.value = document
    attachmentForm.fileName = ''
    attachmentForm.fileUrl = ''
    attachmentForm.secrecyLevel = document.secrecyLevel || '公开'
    attachments.value = await api.attachments({ bizType: 'document', bizId: document.id })
    attachmentDialog.value = true
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}
/** 打开流程导览弹窗 */
const openFlowGuide = (document) => {
  flowGuideDialog.value?.open('document', document.id)
}
/** 添加公文附件 */
const addAttachment = async () => {
  try {
    await attachmentFormRef.value.validate()
    await api.addAttachment({
      bizType: 'document',
      bizId: currentDocument.value.id,
      fileName: attachmentForm.fileName,
      fileUrl: attachmentForm.fileUrl,
      secrecyLevel: attachmentForm.secrecyLevel
    })
    ElMessage.success('附件已保存')
    attachments.value = await api.attachments({ bizType: 'document', bizId: currentDocument.value.id })
  } catch (e) {
    if (e.message !== 'validation failed') {
      ElMessage.error(e.message || '操作失败')
    }
  }
}

onMounted(async () => {
  try {
    userOptions.value = await api.userOptions()
    selectReceiver()
    await load()
  } catch (e) {
    ElMessage.error(e.message || '初始化失败')
  }
})
</script>
