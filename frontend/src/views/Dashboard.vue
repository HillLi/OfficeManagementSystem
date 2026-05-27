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
import { api } from '../api'
import { useDictionaryStore } from '../stores/dictionary'

use([BarChart, GaugeChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const dictionaryStore = useDictionaryStore()
const labelOf = dictionaryStore.labelOf
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
  const data = await api.dashboard()
  Object.assign(stats, data)
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
</script>
