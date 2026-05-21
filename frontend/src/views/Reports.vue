<template>
  <div class="page-grid">
    <div class="panel">
      <h3>请示报告</h3>
      <el-form label-position="top">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type">
            <el-option value="请示" />
            <el-option value="报告" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="6" /></el-form-item>
      </el-form>
      <el-button type="primary" @click="submit">提交</el-button>
    </div>
    <div class="panel">
      <h3>列表</h3>
      <el-table :data="rows" border>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="type" label="类型" width="80" />
        <el-table-column prop="secrecyLevel" label="密级" width="90" />
        <el-table-column prop="status" label="状态" width="130" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-if="row.status === 'approved'" size="small" type="success" @click="reply(row)">批复归档</el-button>
          </template>
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
const form = reactive({
  title: '关于系统上线试运行资源支持的请示',
  type: '请示',
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
onMounted(load)
</script>
