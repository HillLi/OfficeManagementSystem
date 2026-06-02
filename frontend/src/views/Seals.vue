<template>
  <div class="seal-page">
    <div class="panel report-header">
      <h3>用印管理</h3>
    </div>
    <div class="page-actions">
      <div class="header-actions">
        <el-button type="primary" @click="applicationDialog = true">用印申请</el-button>
        <el-button v-if="canManage" @click="transferDialog = true">印章移交</el-button>
      </div>
    </div>

    <el-table :data="apps" border>
      <el-table-column prop="sealName" label="印章名称" min-width="178" />
      <el-table-column prop="purpose" label="用途" min-width="150" />
      <el-table-column prop="materialCount" label="材料" width="72">
        <template #default="{ row }">{{ row.materialCount }} 份</template>
      </el-table-column>
      <el-table-column label="事项等级" width="96"><template #default="{ row }">{{ labelOf('matter_level', row.matterLevel) }}</template></el-table-column>
      <el-table-column label="外带" width="62"><template #default="{ row }">{{ row.takeOut ? '是' : '否' }}</template></el-table-column>
      <el-table-column label="状态" width="116"><template #default="{ row }">{{ labelOf('business_status', row.status) }}</template></el-table-column>
      <el-table-column label="办理" min-width="260">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button size="small" @click="openMaterials(row)">材料管理</el-button>
            <el-button v-if="canSubmit(row)" size="small" type="primary"
              :disabled="row.materialCount === 0" @click="submitDraft(row)">提交审批</el-button>
            <el-button v-if="canManage && row.status === 'approved'" size="small" type="primary" @click="markUsed(row.id)">登记用印</el-button>
            <el-button v-if="canManage && row.status === 'used'" size="small" type="success" @click="markReturned(row.id)">确认归还</el-button>
            <el-button size="small" @click="openFlowGuide(row)">流程导览</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <section v-if="canManage" class="section-block">
      <h3>移交记录</h3>
      <el-table :data="transfers" border>
        <el-table-column label="印章名称" min-width="170">
          <template #default="{ row }">{{ sealName(row.sealId) }}</template>
        </el-table-column>
        <el-table-column label="接收人" width="112"><template #default="{ row }">{{ userName(row.receiverId) }}</template></el-table-column>
        <el-table-column label="监督人" width="112"><template #default="{ row }">{{ userName(row.supervisorId) }}</template></el-table-column>
        <el-table-column prop="materialUrl" label="移交凭证" min-width="120" />
        <el-table-column prop="transferTime" label="移交时间" width="172" />
      </el-table>
    </section>

    <el-dialog v-model="applicationDialog" title="用印申请" width="620px" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="印章">
          <el-select v-model="form.sealId">
            <el-option v-for="seal in seals" :key="seal.id" :label="seal.sealName" :value="seal.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="用途"><el-input v-model="form.purpose" /></el-form-item>
        <el-form-item label="份数"><el-input-number v-model="form.copies" :min="1" /></el-form-item>
        <el-form-item label="事项等级">
          <el-select v-model="form.matterLevel">
            <el-option v-for="level in optionsOf('matter_level')" :key="level.value" :label="level.label" :value="level.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否外带">
          <el-switch v-model="form.takeOut" active-text="外带" inactive-text="在馆使用" />
        </el-form-item>
        <template v-if="form.takeOut">
          <el-form-item label="外带原因"><el-input v-model="form.takeOutReason" /></el-form-item>
          <el-form-item label="使用地点"><el-input v-model="form.takeOutLocation" /></el-form-item>
          <el-form-item label="监督人">
            <el-select v-model="form.supervisorId" filterable>
              <el-option v-for="user in userOptions" :key="user.id" :label="user.realName" :value="user.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="预计归还时间">
            <el-date-picker v-model="form.expectedReturnTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
          </el-form-item>
        </template>
      </el-form>
      <p class="rule-note">申请须上传真实材料文件后方可提交审批。</p>
      <p v-if="form.takeOut" class="rule-note">外带用印须登记监督人、地点及预计归还时间。</p>
      <template #footer>
        <el-button @click="applicationDialog = false">取消</el-button>
        <el-button type="primary" @click="saveDraft">保存草稿并上传材料</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="transferDialog" title="印章移交" width="560px" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="印章">
          <el-select v-model="transferForm.sealId">
            <el-option v-for="seal in seals" :key="seal.id" :label="seal.sealName" :value="seal.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="接收人">
          <el-select v-model="transferForm.receiverId" filterable>
            <el-option v-for="user in userOptions" :key="user.id" :label="user.realName" :value="user.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="监督人">
          <el-select v-model="transferForm.supervisorId" filterable>
            <el-option v-for="user in userOptions" :key="user.id" :label="user.realName" :value="user.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="移交凭证地址"><el-input v-model="transferForm.materialUrl" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="transferForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferDialog = false">取消</el-button>
        <el-button type="primary" @click="createTransfer">登记移交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="materialDialog" :title="materialTitle" width="min(900px, calc(100vw - 24px))" :close-on-click-modal="false">
      <el-alert v-if="currentApplication?.materialUrl && materials.length === 0" type="warning" :closable="false"
        title="历史申请保留了原材料地址记录，请按档案核验；新增材料请使用文件上传。" />
      <div v-if="canUpload" class="upload-row">
        <el-upload :auto-upload="false" :limit="1" :file-list="uploadFiles" accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
          :on-change="chooseFile" :on-remove="removeFile">
          <el-button>选择文件</el-button>
        </el-upload>
        <el-select v-model="uploadSecrecy" class="secrecy-select" aria-label="材料密级">
          <el-option v-for="level in optionsOf('secrecy_level')" :key="level.value" :label="level.label" :value="level.value" />
        </el-select>
        <el-button type="primary" :disabled="!selectedFile" @click="uploadMaterial">上传材料</el-button>
      </div>
      <p v-if="canUpload" class="rule-note">支持 PDF、DOC、DOCX、JPG、JPEG、PNG，单个文件不超过 20 MB。</p>
      <el-checkbox v-if="canViewDeleted" v-model="includeDeleted" @change="loadMaterials">显示已删除材料</el-checkbox>
      <el-table :data="materials" border class="material-table">
        <el-table-column prop="fileName" label="材料名称" min-width="190" />
        <el-table-column label="密级" width="82"><template #default="{ row }">{{ labelOf('secrecy_level', row.secrecyLevel) }}</template></el-table-column>
        <el-table-column label="大小" width="86"><template #default="{ row }">{{ fileSize(row.fileSize) }}</template></el-table-column>
        <el-table-column prop="createdAt" label="上传时间" width="168" />
        <el-table-column label="状态" width="76"><template #default="{ row }">{{ row.deleted ? '已删除' : '有效' }}</template></el-table-column>
        <el-table-column label="办理" min-width="208">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button v-if="!row.deleted" size="small" @click="downloadMaterial(row)">下载</el-button>
              <el-button v-if="canEdit(row)" size="small" @click="openEdit(row)">编辑</el-button>
              <el-button v-if="canDelete(row)" size="small" type="danger" @click="removeMaterial(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="editDialog" title="修改材料信息" width="min(430px, calc(100vw - 24px))" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="材料名称"><el-input v-model="editForm.fileName" /></el-form-item>
        <el-form-item label="密级">
          <el-select v-model="editForm.secrecyLevel">
            <el-option v-for="level in optionsOf('secrecy_level')" :key="level.value" :label="level.label" :value="level.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="editDialog = false">取消</el-button><el-button type="primary" @click="saveEdit">保存</el-button></template>
    </el-dialog>
    <WorkflowGuideDialog ref="flowGuideDialog" />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import { useDictionaryStore } from '../stores/dictionary'
import WorkflowGuideDialog from '../components/WorkflowGuideDialog.vue'

const dictionaryStore = useDictionaryStore()
const labelOf = dictionaryStore.labelOf
const optionsOf = dictionaryStore.optionsOf
const currentUser = JSON.parse(sessionStorage.getItem('oms_user') || '{"id":0,"roleKeys":[]}')
const canManage = computed(() => currentUser.roleKeys?.some((role) => ['seal_keeper', 'office_admin', 'admin'].includes(role)))
const canViewDeleted = computed(() => currentUser.roleKeys?.some((role) => ['office_admin', 'admin'].includes(role)))
const seals = ref([])
const userOptions = ref([])
const apps = ref([])
const transfers = ref([])
const materials = ref([])
const currentApplication = ref(null)
const applicationDialog = ref(false)
const transferDialog = ref(false)
const materialDialog = ref(false)
const editDialog = ref(false)
const includeDeleted = ref(false)
const uploadFiles = ref([])
const selectedFile = ref(null)
const uploadSecrecy = ref('内部')
const flowGuideDialog = ref(null)
const form = reactive({
  sealId: 1,
  applicantId: currentUser.id || 2,
  purpose: '系统试运行通知材料用印',
  copies: 2,
  takeOut: false,
  matterLevel: '常规事项',
  takeOutReason: '',
  takeOutLocation: '',
  supervisorId: null,
  expectedReturnTime: ''
})
const transferForm = reactive({
  sealId: 1,
  receiverId: 2,
  supervisorId: currentUser.id || 2,
  materialUrl: '/files/seal-transfer.pdf',
  remark: ''
})
const editForm = reactive({ id: null, fileName: '', secrecyLevel: '内部' })
const materialTitle = computed(() => currentApplication.value ? `${currentApplication.value.sealName} - 用印材料` : '用印材料')
const canUpload = computed(() => currentApplication.value?.status === 'draft'
  && currentApplication.value?.applicantId === currentUser.id)

const sealName = (sealId) => seals.value.find((seal) => seal.id === sealId)?.sealName || `印章 #${sealId}`
const userName = (userId) => userOptions.value.find((user) => user.id === userId)?.realName || `#${userId}`
const canSubmit = (row) => row.status === 'draft' && row.applicantId === currentUser.id
const canEdit = (row) => !row.deleted && canUpload.value
const canDelete = (row) => !row.deleted && (canUpload.value || canViewDeleted.value)
const fileSize = (bytes) => bytes == null ? '-' : bytes < 1024 ? `${bytes} B` : `${(bytes / 1024).toFixed(1)} KB`

const load = async () => {
  const [sealRows, appRows, optionRows] = await Promise.all([api.seals(), api.sealApps(), api.userOptions()])
  seals.value = sealRows
  apps.value = appRows
  userOptions.value = optionRows
  if (canManage.value) transfers.value = await api.sealTransfers()
}
const saveDraft = async () => {
  try {
    const created = await api.createSealApp(form)
    ElMessage.success('草稿已保存，请上传材料后提交审批')
    applicationDialog.value = false
    await load()
    await openMaterials(created)
  } catch (error) {
    ElMessage.error(error.message || '保存草稿失败')
  }
}
const submitDraft = async (row) => {
  await api.submitSealApp(row.id)
  ElMessage.success('用印申请已提交审批')
  await load()
  if (currentApplication.value?.id === row.id) {
    currentApplication.value = apps.value.find((application) => application.id === row.id)
  }
}
const openMaterials = async (application) => {
  currentApplication.value = application
  includeDeleted.value = false
  uploadFiles.value = []
  selectedFile.value = null
  await loadMaterials()
  materialDialog.value = true
}
const openFlowGuide = (application) => {
  flowGuideDialog.value?.open('seal', application.id)
}
const loadMaterials = async () => {
  if (!currentApplication.value) return
  materials.value = await api.attachments({
    bizType: 'seal',
    bizId: currentApplication.value.id,
    includeDeleted: includeDeleted.value
  })
}
const chooseFile = (uploadFile, fileList) => {
  selectedFile.value = uploadFile.raw
  uploadFiles.value = fileList.slice(-1)
}
const removeFile = () => {
  selectedFile.value = null
  uploadFiles.value = []
}
const uploadMaterial = async () => {
  const data = new FormData()
  data.append('bizType', 'seal')
  data.append('bizId', String(currentApplication.value.id))
  data.append('secrecyLevel', uploadSecrecy.value)
  data.append('file', selectedFile.value)
  await api.uploadAttachment(data)
  ElMessage.success('材料上传成功')
  removeFile()
  await loadMaterials()
  await load()
  currentApplication.value = apps.value.find((application) => application.id === currentApplication.value.id)
}
const downloadMaterial = async (row) => {
  const blob = await api.downloadAttachment(row.id)
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = row.originalName || row.fileName
  link.click()
  URL.revokeObjectURL(link.href)
}
const openEdit = (row) => {
  editForm.id = row.id
  editForm.fileName = row.fileName
  editForm.secrecyLevel = row.secrecyLevel
  editDialog.value = true
}
const saveEdit = async () => {
  await api.updateAttachment(editForm.id, { fileName: editForm.fileName, secrecyLevel: editForm.secrecyLevel })
  editDialog.value = false
  ElMessage.success('材料信息已更新')
  await loadMaterials()
}
const removeMaterial = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入删除原因', `删除材料：${row.fileName}`, {
      inputPattern: /\S+/,
      inputErrorMessage: '删除原因不能为空',
      type: 'warning'
    })
    await api.deleteAttachment(row.id, { reason: value })
    ElMessage.success('材料已逻辑删除')
    await loadMaterials()
    await load()
    currentApplication.value = apps.value.find((application) => application.id === currentApplication.value.id)
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(error.message || '删除失败')
  }
}
const markUsed = async (id) => {
  await api.markSealUsed(id, currentUser.id)
  ElMessage.success('用印状态已登记')
  await load()
}
const markReturned = async (id) => {
  await api.returnSeal(id, currentUser.id)
  ElMessage.success('归还状态已确认')
  await load()
}
const createTransfer = async () => {
  await api.createSealTransfer(transferForm)
  ElMessage.success('印章移交已登记')
  transferDialog.value = false
  await load()
}

onMounted(load)
</script>

<style scoped>
.header-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.section-block {
  margin-top: 18px;
}
.section-block h3 {
  margin: 0 0 10px;
  font-size: 16px;
}
.upload-row {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 8px;
}
.secrecy-select {
  width: 118px;
}
.material-table {
  margin-top: 12px;
}
@media (max-width: 600px) {
  .upload-row {
    flex-wrap: wrap;
  }
}
</style>
