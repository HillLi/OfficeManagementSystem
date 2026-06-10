<template>
  <div class="report-page">
    <div class="panel report-header">
      <h3>请示报告</h3>
    </div>
    <div class="page-actions">
      <el-button type="primary" @click="applicationDialog = true">提交请示报告</el-button>
    </div>

    <el-table :data="rows" border v-loading="loading">
      <template #empty><el-empty description="暂无请示报告" /></template>
      <el-table-column prop="title" label="标题" />
      <el-table-column label="类型" width="80"><template #default="{ row }">{{ labelOf('report_type', row.type) }}</template></el-table-column>
      <el-table-column label="密级" width="90"><template #default="{ row }">{{ labelOf('secrecy_level', row.secrecyLevel) }}</template></el-table-column>
      <el-table-column label="状态" width="130"><template #default="{ row }">{{ labelOf('business_status', row.status) }}</template></el-table-column>
      <el-table-column label="操作" width="190">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button v-if="row.status === 'approved'" size="small" type="success" @click="reply(row)">批复归档</el-button>
            <el-button size="small" @click="openFlowGuide(row)">流程导览</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="applicationDialog" title="提交请示报告" width="560px" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type"><el-option v-for="item in optionsOf('report_type')" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        </el-form-item>
        <el-form-item label="密级">
          <el-select v-model="form.secrecyLevel"><el-option v-for="item in optionsOf('secrecy_level')" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="6" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applicationDialog = false">取消</el-button>
        <el-button type="primary" @click="submit">提交</el-button>
      </template>
    </el-dialog>
    <WorkflowGuideDialog ref="flowGuideDialog" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import { useDictionaryStore } from '../stores/dictionary'
import WorkflowGuideDialog from '../components/WorkflowGuideDialog.vue'

const dictionaryStore = useDictionaryStore()
const labelOf = dictionaryStore.labelOf
const optionsOf = dictionaryStore.optionsOf
const rows = ref([])
const applicationDialog = ref(false)
const flowGuideDialog = ref(null)
const loading = ref(false)
const form = reactive({
  title: '',
  type: '',
  secrecyLevel: '公开',
  content: '',
  applicantId: null
})
const load = async () => {
  loading.value = true
  try {
    rows.value = await api.reports()
  } catch (e) {
    ElMessage.error(e.message || '加载报告数据失败')
  } finally {
    loading.value = false
  }
}
const submit = async () => {
  try {
    await api.createReport(form)
    ElMessage.success('已提交')
    applicationDialog.value = false
    await load()
  } catch (e) {
    ElMessage.error(e.message || '提交失败')
  }
}
const reply = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入批复意见', '批复归档', {
      inputType: 'textarea',
      inputValue: row.reply || '同意按流程办理。'
    })
    await api.replyReport(row.id, { reply: value })
    ElMessage.success('已批复归档')
    await load()
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error(e.message || '批复失败')
    }
  }
}
const openFlowGuide = (row) => {
  flowGuideDialog.value?.open('report', row.id)
}
onMounted(load)
</script>
