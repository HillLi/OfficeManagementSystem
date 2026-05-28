<template>
  <div class="report-page">
    <h2 class="page-title">请示报告</h2>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="请示报告" name="application">
        <div class="tab-form">
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
          <el-button type="primary" @click="submit">提交</el-button>
        </div>
      </el-tab-pane>

      <el-tab-pane label="办理记录" name="records">
        <el-table :data="rows" border>
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
      </el-tab-pane>
    </el-tabs>
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
const activeTab = ref('application')
const rows = ref([])
const flowGuideDialog = ref(null)
const form = reactive({
  title: '关于系统上线试运行资源支持的请示',
  type: '请示',
  secrecyLevel: '内部',
  content: '拟申请相关服务器资源和测试账号支持。',
  applicantId: 2
})
const load = async () => { rows.value = await api.reports() }
const submit = async () => {
  await api.createReport(form)
  ElMessage.success('已提交')
  load()
}
const reply = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入批复意见', '批复归档', {
    inputType: 'textarea',
    inputValue: row.reply || '同意按流程办理。'
  })
  await api.replyReport(row.id, { reply: value })
  ElMessage.success('已批复归档')
  load()
}
const openFlowGuide = (row) => {
  flowGuideDialog.value?.open('report', row.id)
}
onMounted(load)
</script>

<style scoped>
.page-title {
  margin-top: 0;
}
.tab-form {
  max-width: 520px;
}
</style>
