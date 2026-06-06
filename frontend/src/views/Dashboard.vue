<template>
  <div>
    <div class="grid">
      <div class="panel"><h3>公文总数</h3><div class="metric">{{ stats.documentCount }}</div></div>
      <div class="panel"><h3>待办公文</h3><div class="metric">{{ stats.pendingDocumentCount }}</div></div>
      <div class="panel"><h3>用印申请</h3><div class="metric">{{ stats.sealApplyCount }}</div></div>
      <div class="panel"><h3>会议申请</h3><div class="metric">{{ stats.meetingCount }}</div></div>
      <div class="panel"><h3>差旅申请</h3><div class="metric">{{ stats.travelCount }}</div></div>
      <div class="panel"><h3>大型活动</h3><div class="metric">{{ stats.largeActivityCount }}</div></div>
    </div>

    <div class="panel announcement-panel">
      <div class="panel-title">
        <h3>最新通知公告</h3>
        <el-button link type="primary" @click="$router.push('/announcements')">查看全部</el-button>
      </div>
      <el-empty v-if="latestAnnouncements.length === 0" description="暂无通知公告" />
      <div v-else class="dashboard-announcements">
        <div v-for="item in latestAnnouncements" :key="item.id" class="announcement-row">
          <div>
            <el-tag v-if="item.pinned" type="danger" size="small">置顶</el-tag>
            <button type="button" class="announcement-title-link" @click="openAnnouncement(item)">{{ item.title }}</button>
            <p>{{ item.content }}</p>
          </div>
          <span>{{ formatDate(item.publishedAt || item.updatedAt || item.createdAt) }}</span>
        </div>
      </div>
    </div>

    <div class="chart-grid">
      <div class="panel">
        <h3>公文状态分布</h3>
        <div v-if="hasDocumentStatusData" ref="pieRef" style="height:300px"></div>
        <el-empty v-else class="chart-empty" description="暂无数据" />
      </div>
      <div class="panel">
        <h3>月度业务量</h3>
        <div v-if="hasMonthlyBusinessData" ref="barRef" style="height:300px"></div>
        <el-empty v-else class="chart-empty" description="暂无数据" />
      </div>
      <div class="panel">
        <h3>差旅预算概览</h3>
        <div v-if="hasTravelBudgetData" ref="gaugeRef" style="height:300px"></div>
        <el-empty v-else class="chart-empty" description="暂无数据" />
      </div>
    </div>

    <el-dialog
      v-model="detailVisible"
      title="公告详情"
      width="720px"
      :close-on-click-modal="false"
    >
      <article v-if="selectedAnnouncement" class="announcement-detail">
        <div class="detail-meta">
          <el-tag v-if="selectedAnnouncement.pinned" type="danger" size="small">置顶</el-tag>
          <el-tag size="small">{{ categoryText(selectedAnnouncement.category) }}</el-tag>
          <span>发布范围：{{ scopeText(selectedAnnouncement) }}</span>
          <span>{{ formatDate(selectedAnnouncement.publishedAt || selectedAnnouncement.updatedAt || selectedAnnouncement.createdAt) }}</span>
        </div>
        <h2>{{ selectedAnnouncement.title }}</h2>
        <div class="detail-content">{{ selectedAnnouncement.content }}</div>
      </article>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { BarChart, GaugeChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { init, use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { api } from '../api'
import { useDictionaryStore } from '../stores/dictionary'

use([BarChart, GaugeChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const dictionaryStore = useDictionaryStore()
const labelOf = dictionaryStore.labelOf
const latestAnnouncements = ref([])
const detailVisible = ref(false)
const selectedAnnouncement = ref(null)
const stats = reactive({
  documentCount: 0, pendingDocumentCount: 0, sealApplyCount: 0,
  meetingCount: 0, travelCount: 0, reportCount: 0, largeActivityCount: 0,
  travelBudgetTotal: 0, documentStatusDistribution: {}, monthlyBusinessCounts: {}
})

const pieRef = ref(null)
const barRef = ref(null)
const gaugeRef = ref(null)
let charts = []

const hasDocumentStatusData = computed(() =>
  Object.keys(stats.documentStatusDistribution || {}).length > 0
)
const hasMonthlyBusinessData = computed(() =>
  Object.keys(stats.monthlyBusinessCounts || {}).length > 0
)
const hasTravelBudgetData = computed(() =>
  Number(stats.travelBudgetTotal) > 0
)

onMounted(async () => {
  const [dashboardData, announcements] = await Promise.all([
    api.dashboard(),
    api.latestAnnouncements({ limit: 5 })
  ])
  Object.assign(stats, dashboardData)
  latestAnnouncements.value = announcements
  initCharts()
})

onUnmounted(() => {
  charts.forEach(c => c.dispose())
})

function initCharts() {
  const statusDist = stats.documentStatusDistribution || {}
  if (hasDocumentStatusData.value && pieRef.value) {
    const pie = init(pieRef.value)
    charts.push(pie)
    pie.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie', radius: ['40%', '70%'],
        data: Object.entries(statusDist).map(([name, value]) => ({ name: labelOf('business_status', name), value })),
        emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.3)' } }
      }]
    })
  }

  const monthly = stats.monthlyBusinessCounts || {}
  if (hasMonthlyBusinessData.value && barRef.value) {
    const bar = init(barRef.value)
    charts.push(bar)
    bar.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: Object.keys(monthly) },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: Object.values(monthly), itemStyle: { color: '#1f5f8b' } }]
    })
  }

  if (hasTravelBudgetData.value && gaugeRef.value) {
    const gauge = init(gaugeRef.value)
    charts.push(gauge)
    gauge.setOption({
      series: [{
        type: 'gauge',
        detail: { formatter: '{value}元' },
        data: [{ value: Number(stats.travelBudgetTotal) || 0, name: '差旅预算总额' }]
      }]
    })
  }
}

function formatDate(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '-'
}

function openAnnouncement(item) {
  selectedAnnouncement.value = item
  detailVisible.value = true
}

function categoryText(category) {
  return { notice: '通知', announcement: '公告', policy: '制度' }[category] || '通知'
}

function scopeText(row) {
  return row.targetType === 'dept' ? (row.targetDeptName || '指定部门') : '全校'
}
</script>

<style scoped>
.announcement-panel {
  margin-top: 14px;
}

.panel-title,
.announcement-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}

.panel-title h3 {
  margin: 0;
}

.dashboard-announcements {
  display: grid;
  gap: 10px;
}

.announcement-row {
  border-top: 1px solid #eef2f6;
  padding-top: 10px;
}

.announcement-title-link {
  appearance: none;
  border: 0;
  background: transparent;
  padding: 0;
  margin-left: 6px;
  color: #1f5f8b;
  cursor: pointer;
  font: inherit;
  font-weight: 700;
  text-decoration: none;
}

.announcement-title-link:hover {
  text-decoration: underline;
}

.announcement-row p {
  margin: 6px 0 0;
  color: #657487;
  max-width: 760px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.announcement-row span {
  color: #657487;
  white-space: nowrap;
}

.announcement-detail h2 {
  margin: 14px 0 16px;
  font-size: 22px;
  line-height: 1.4;
  letter-spacing: 0;
}

.detail-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  color: #657487;
}

.detail-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #303946;
}

.chart-empty {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 700px) {
  .panel-title,
  .announcement-row {
    flex-direction: column;
  }
}
</style>
