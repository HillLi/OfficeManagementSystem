<template>
  <div class="page-grid">
    <div class="panel">
      <h3>差旅申请</h3>
      <el-form label-position="top">
        <el-form-item label="目的地"><el-input v-model="form.destination" /></el-form-item>
        <el-form-item label="出发日期"><el-date-picker v-model="form.startDate" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="返回日期"><el-date-picker v-model="form.endDate" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="预算"><el-input-number v-model="form.budget" :min="0" /></el-form-item>
      </el-form>
      <el-button type="primary" @click="submit">提交差旅</el-button>
    </div>
    <div class="panel">
      <h3>差旅列表</h3>
      <el-table :data="rows" border>
        <el-table-column prop="destination" label="目的地" />
        <el-table-column prop="budget" label="预算" width="100" />
        <el-table-column label="标准" width="100"><template #default="{ row }">{{ row.checkResult?.standardAmount }}</template></el-table-column>
        <el-table-column prop="status" label="状态" width="130" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'

const rows = ref([])
const form = reactive({ applicantId: 2, destination: '上海', startDate: '2026-06-01', endDate: '2026-06-03', reason: '参加高校信息化建设会议', budget: 2600 })
const load = async () => { rows.value = await api.travels() }
const submit = async () => { await api.createTravel(form); ElMessage.success('已提交'); load() }
onMounted(load)
</script>
