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
            <button type="button" class="announcement-title-btn" @click="openAnnouncement(item)">{{ item.title }}</button>
            <p>{{ item.content }}</p>
          </div>
          <span>{{ formatDate(item.publishedAt || item.updatedAt || item.createdAt) }}</span>
        </div>
      </div>
    </div>

    <div class="chart-grid">
      <div class="panel">
        <h3>公文状态分布</h3>
        <div ref="pieRef" style="height:300px"></div>
      </div>
      <div class="panel">
        <h3>月度业务量</h3>
        <div ref="barRef" style="height:300px"></div>
      </div>
      <div class="panel">
        <h3>差旅预算概览</h3>
        <div ref="gaugeRef" style="height:300px"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { BarChart, GaugeChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { init, use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { useDictionaryStore } from '../stores/dictionary'

use([BarChart, GaugeChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const dictionaryStore = useDictionaryStore()
const router = useRouter()
const labelOf = dictionaryStore.labelOf
const latestAnnouncements = ref([])
const stats = reactive({
  documentCount: 0, pendingDocumentCount: 0, sealApplyCount: 0,
  meetingCount: 0, travelCount: 0, reportCount: 0, largeActivityCount: 0,
  travelBudgetTotal: 0, documentStatusDistribution: {}, monthlyBusinessCounts: {}
})

const pieRef = ref(null)
const barRef = ref(null)
const gaugeRef = ref(null)
let charts = []

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
  if (Object.keys(statusDist).length > 0) {
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
  if (Object.keys(monthly).length > 0) {
    const bar = init(barRef.value)
    charts.push(bar)
    bar.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: Object.keys(monthly) },
      yAxis: { type: 'value' },
      series: [{ type: 'bar', data: Object.values(monthly), itemStyle: { color: '#1f5f8b' } }]
    })
  }

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

function formatDate(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '-'
}

function openAnnouncement(item) {
  router.push({ path: '/announcements', query: { focus: item.id } })
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

.announcement-title-btn {
  appearance: none;
  border: 0;
  background: transparent;
  padding: 0;
  margin-left: 6px;
  color: #1f5f8b;
  cursor: pointer;
  font: inherit;
  font-weight: 700;
}

.announcement-title-btn:hover {
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

@media (max-width: 700px) {
  .panel-title,
  .announcement-row {
    flex-direction: column;
  }
}
</style>
