<template>
  <div class="page-grid">
    <div class="panel">
      <h3>公文起草</h3>
      <el-form label-position="top">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="文种">
          <el-select v-model="form.docType"><el-option v-for="item in docTypes" :key="item" :value="item" /></el-select>
        </el-form-item>
        <el-form-item label="密级">
          <el-select v-model="form.secrecyLevel"><el-option v-for="item in levels" :key="item" :value="item" /></el-select>
        </el-form-item>
        <el-form-item label="正文"><el-input v-model="form.content" type="textarea" :rows="6" /></el-form-item>
      </el-form>
      <div class="toolbar">
        <el-button type="primary" @click="submit">保存草稿</el-button>
        <el-button @click="draft">AI起草</el-button>
      </div>
    </div>
    <div class="panel">
      <h3>公文列表</h3>
      <el-table :data="rows" border>
        <el-table-column prop="title" label="标题" min-width="220" />
        <el-table-column prop="docType" label="文种" width="90" />
        <el-table-column prop="secrecyLevel" label="密级" width="90" />
        <el-table-column prop="status" label="状态" width="130" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }"><el-button size="small" @click="review(row.id)">AI审核</el-button></template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'

const rows = ref([])
const docTypes = ['通知', '决定', '请示', '批复', '报告', '函', '公告']
const levels = ['公开', '内部', '秘密', '机密', '绝密']
const form = reactive({
  title: '关于开展办公管理系统试运行的通知',
  docType: '通知',
  secrecyLevel: '公开',
  content: '各单位：为提高学校办公流程办理效率，现组织开展系统试运行工作，请各单位按要求反馈使用情况。',
  applicantId: 2
})

const load = async () => { rows.value = await api.documents() }
const submit = async () => { await api.createDocument(form); ElMessage.success('已保存'); load() }
const draft = async () => {
  form.content = await api.aiDraft({ docType: form.docType, topic: '办公管理系统试运行', keyPoints: '明确试运行范围、反馈方式和时间要求。' })
}
const review = async (id) => {
  const result = await api.aiReview(id)
  ElMessageBox.alert(result.issues.concat(result.suggestions).join('\n') || '审核通过', 'AI审核结果')
}
onMounted(load)
</script>
