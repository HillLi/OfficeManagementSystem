<template>
  <div>
    <div class="panel report-header">
      <h3>统计报表</h3>
      <el-button type="primary" @click="download">导出 CSV</el-button>
    </div>
    <div class="grid" style="margin-top: 14px">
      <div class="panel"><h3>公文办理</h3><div class="metric">{{ stats.documentCount }}</div></div>
      <div class="panel"><h3>待办公文</h3><div class="metric">{{ stats.pendingDocumentCount }}</div></div>
      <div class="panel"><h3>用印申请</h3><div class="metric">{{ stats.sealApplyCount }}</div></div>
      <div class="panel"><h3>会议申请</h3><div class="metric">{{ stats.meetingCount }}</div></div>
      <div class="panel"><h3>大型活动</h3><div class="metric">{{ stats.largeActivityCount }}</div></div>
      <div class="panel"><h3>差旅申请</h3><div class="metric">{{ stats.travelCount }}</div></div>
      <div class="panel"><h3>请示报告</h3><div class="metric">{{ stats.reportCount }}</div></div>
      <div class="panel"><h3>差旅预算总额</h3><div class="metric">{{ stats.travelBudgetTotal }}</div></div>
    </div>
    <div class="page-grid" style="margin-top: 14px">
      <div class="panel">
        <h3>公文状态</h3>
        <el-table :data="documentStatusRows" border>
          <el-table-column prop="status" label="状态" />
          <el-table-column prop="count" label="数量" width="90" />
        </el-table>
      </div>
      <div class="panel">
        <h3>月度业务量</h3>
        <el-table :data="monthlyRows" border>
          <el-table-column prop="month" label="月份" />
          <el-table-column prop="count" label="数量" width="90" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'

const stats = reactive({
  documentCount: 0,
  pendingDocumentCount: 0,
  sealApplyCount: 0,
  meetingCount: 0,
  travelCount: 0,
  reportCount: 0,
  largeActivityCount: 0,
  travelBudgetTotal: 0,
  documentStatusDistribution: {},
  monthlyBusinessCounts: {}
})

const documentStatusRows = computed(() =>
  Object.entries(stats.documentStatusDistribution || {}).map(([status, count]) => ({ status, count }))
)
const monthlyRows = computed(() =>
  Object.entries(stats.monthlyBusinessCounts || {}).map(([month, count]) => ({ month, count }))
)

onMounted(async () => Object.assign(stats, await api.statistics()))

const download = async () => {
  const blob = await api.exportStatistics()
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'statistics.csv'
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('统计报表已导出')
}
</script>

<style scoped>
.report-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.report-header h3 {
  margin: 0;
}
</style>
