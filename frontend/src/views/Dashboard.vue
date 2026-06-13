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

    <div class="panel schedule-panel">
      <div class="panel-title">
        <h3>日程管理</h3>
        <span class="schedule-month">{{ scheduleMonthTitle }}</span>
      </div>
      <div class="schedule-layout">
        <div class="calendar-box">
          <div class="calendar-weekdays">
            <span v-for="day in weekdayNames" :key="day">{{ day }}</span>
          </div>
          <div class="calendar-days">
            <button
              v-for="day in calendarDays"
              :key="day.key"
              type="button"
              class="calendar-day"
              :class="{
                'is-empty': day.empty,
                'is-today': day.isToday,
                'is-selected': day.key === selectedDateKey
              }"
              :disabled="day.empty"
              @click="selectDate(day.key)"
            >
              <span>{{ day.label }}</span>
              <span v-if="!day.empty && scheduleTypeForDay(day.key)" class="schedule-dots">
                <i v-if="scheduleTypeForDay(day.key).meeting" class="dot meeting-dot"></i>
                <i v-if="scheduleTypeForDay(day.key).activity" class="dot activity-dot"></i>
              </span>
            </button>
          </div>
        </div>

        <div class="schedule-list">
          <div class="schedule-list-title">
            <h4>{{ selectedDateLabel }}</h4>
            <span>本月 {{ monthlyScheduleItems.length }} 项</span>
          </div>
          <el-empty
            v-if="selectedDayItems.length === 0"
            :description="monthlyScheduleItems.length === 0 ? '本月暂无会议或活动' : '当日暂无会议或活动'"
          />
          <div v-else class="schedule-items">
            <div v-for="item in selectedDayItems" :key="item.bizType + '-' + item.id" class="schedule-item">
              <div>
                <el-tag :type="item.largeActivity ? 'warning' : 'primary'" size="small">
                  {{ item.typeText || (item.largeActivity ? '大型活动' : '会议') }}
                </el-tag>
                <strong>{{ item.title }}</strong>
              </div>
              <p>{{ formatScheduleTime(item) }}</p>
              <p>{{ item.roomName || '未指定会议室' }} · {{ labelOf('business_status', item.status) }}</p>
            </div>
          </div>
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

// 系统首页仪表盘：展示统计概览、最新公告、日程日历和数据图表
<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue'
import { BarChart, GaugeChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { init, use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { api } from '../api'
import { useDictionaryStore } from '../stores/dictionary'
import { formatDate } from '../utils/format'

// 注册ECharts所需组件
use([BarChart, GaugeChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const dictionaryStore = useDictionaryStore()
const labelOf = dictionaryStore.labelOf
const latestAnnouncements = ref([])
const detailVisible = ref(false)
const selectedAnnouncement = ref(null)
const monthlyScheduleItems = ref([])
const selectedDateKey = ref(dateKey(new Date()))
const weekdayNames = ['一', '二', '三', '四', '五', '六', '日']

// 仪表盘统计数据
const stats = reactive({
  documentCount: 0, pendingDocumentCount: 0, sealApplyCount: 0,
  meetingCount: 0, travelCount: 0, reportCount: 0, largeActivityCount: 0,
  travelBudgetTotal: 0, documentStatusDistribution: {}, monthlyBusinessCounts: {}
})

// ECharts图表DOM引用
const pieRef = ref(null)
const barRef = ref(null)
const gaugeRef = ref(null)
let charts = []

// 是否有公文状态分布数据
const hasDocumentStatusData = computed(() =>
  Object.keys(stats.documentStatusDistribution || {}).length > 0
)
// 是否有月度业务量数据
const hasMonthlyBusinessData = computed(() =>
  Object.keys(stats.monthlyBusinessCounts || {}).length > 0
)
// 是否有差旅预算数据
const hasTravelBudgetData = computed(() =>
  Number(stats.travelBudgetTotal) > 0
)

// 当前日程月份的起始日期
const scheduleMonthDate = computed(() => {
  const now = new Date()
  return new Date(now.getFullYear(), now.getMonth(), 1)
})

// 日程月份标题文本
const scheduleMonthTitle = computed(() => {
  const month = scheduleMonthDate.value
  return `${month.getFullYear()} 年 ${month.getMonth() + 1} 月日程`
})

// 计算当前月的日历天数（含前置空位）
const calendarDays = computed(() => {
  const month = scheduleMonthDate.value
  const year = month.getFullYear()
  const monthIndex = month.getMonth()
  const firstDay = new Date(year, monthIndex, 1)
  const leadingDays = (firstDay.getDay() + 6) % 7
  const totalDays = new Date(year, monthIndex + 1, 0).getDate()
  const days = []
  for (let i = 0; i < leadingDays; i++) {
    days.push({ key: `empty-${i}`, label: '', empty: true })
  }
  const today = dateKey(new Date())
  for (let day = 1; day <= totalDays; day++) {
    const current = new Date(year, monthIndex, day)
    const key = dateKey(current)
    days.push({
      key,
      label: day,
      empty: false,
      isToday: key === today
    })
  }
  return days
})

// 按日期分组日程项
const scheduleByDate = computed(() => {
  const grouped = {}
  monthlyScheduleItems.value.forEach((item) => {
    dateRangeKeys(item).forEach((key) => {
      grouped[key] = grouped[key] || []
      grouped[key].push(item)
    })
  })
  Object.values(grouped).forEach((items) => {
    items.sort((a, b) => String(a.startTime || '').localeCompare(String(b.startTime || '')))
  })
  return grouped
})

// 当前选中日期的日程项
const selectedDayItems = computed(() => scheduleByDate.value[selectedDateKey.value] || [])

// 选中日期的显示标签
const selectedDateLabel = computed(() => {
  const date = parseDateKey(selectedDateKey.value)
  return date ? `${date.getMonth() + 1} 月 ${date.getDate()} 日事项` : '当日事项'
})

// 页面挂载时加载仪表盘数据和最新公告
onMounted(async () => {
  try {
    const [dashboardData, announcements] = await Promise.all([
      api.dashboard(),
      api.latestAnnouncements({ limit: 5 })
    ])
    Object.assign(stats, dashboardData)
    monthlyScheduleItems.value = Array.isArray(dashboardData.monthlyScheduleItems) ? dashboardData.monthlyScheduleItems : []
    latestAnnouncements.value = announcements
    // 等待 v-if 控制的图表容器渲染到 DOM 后再初始化 ECharts
    await nextTick()
    initCharts()
  } catch (e) {
    console.error('Dashboard load failed', e)
  }
})

// 页面卸载时销毁ECharts实例，防止内存泄漏
onUnmounted(() => {
  charts.forEach(c => c.dispose())
})

// 初始化ECharts图表：公文状态饼图、月度业务量柱状图、差旅预算仪表盘
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

// 选中某一天
function selectDate(key) {
  selectedDateKey.value = key
}

// 获取某天日程中包含的类型（会议/活动）
function scheduleTypeForDay(key) {
  const items = scheduleByDate.value[key] || []
  if (items.length === 0) {
    return null
  }
  return {
    meeting: items.some((item) => !item.largeActivity),
    activity: items.some((item) => item.largeActivity)
  }
}

// 计算日程项在当前月内占用的所有日期key
function dateRangeKeys(item) {
  if (!item.startTime) {
    return []
  }
  const start = new Date(item.startTime)
  const end = item.endTime ? new Date(item.endTime) : start
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) {
    return []
  }
  if (end.getTime() <= start.getTime()) {
    return []
  }
  const month = scheduleMonthDate.value
  const monthStart = new Date(month.getFullYear(), month.getMonth(), 1)
  const monthEnd = new Date(month.getFullYear(), month.getMonth() + 1, 0)
  const finalOccupiedDate = new Date(end.getTime() - 1)
  const current = new Date(Math.max(startOfDay(start).getTime(), monthStart.getTime()))
  const finalDay = new Date(Math.min(startOfDay(finalOccupiedDate).getTime(), monthEnd.getTime()))
  const keys = []
  while (current <= finalDay) {
    keys.push(dateKey(current))
    current.setDate(current.getDate() + 1)
  }
  return keys
}

// 格式化日程项的起止时间
function formatScheduleTime(item) {
  if (!item.startTime) {
    return '-'
  }
  const start = new Date(item.startTime)
  const end = item.endTime ? new Date(item.endTime) : null
  if (Number.isNaN(start.getTime())) {
    return '-'
  }
  const startText = `${pad(start.getHours())}:${pad(start.getMinutes())}`
  if (!end || Number.isNaN(end.getTime())) {
    return startText
  }
  const endText = `${pad(end.getHours())}:${pad(end.getMinutes())}`
  if (dateKey(start) === dateKey(end)) {
    return `${startText} - ${endText}`
  }
  return `${start.getMonth() + 1}/${start.getDate()} ${startText} - ${end.getMonth() + 1}/${end.getDate()} ${endText}`
}

// 将日期转换为YYYY-MM-DD格式的key
function dateKey(date) {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

// 解析日期key字符串为Date对象
function parseDateKey(key) {
  const parts = String(key || '').split('-').map(Number)
  if (parts.length !== 3 || parts.some((part) => Number.isNaN(part))) {
    return null
  }
  const date = new Date(parts[0], parts[1] - 1, parts[2])
  return date.getFullYear() === parts[0] && date.getMonth() === parts[1] - 1 && date.getDate() === parts[2]
    ? date
    : null
}

// 获取某天的零时Date对象
function startOfDay(date) {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate())
}

// 数字补零
function pad(value) {
  return String(value).padStart(2, '0')
}

// 打开公告详情弹窗
function openAnnouncement(item) {
  selectedAnnouncement.value = item
  detailVisible.value = true
}

// 公告分类文本映射
function categoryText(category) {
  return { notice: '通知', announcement: '公告', policy: '制度' }[category] || '通知'
}

// 公告发布范围文本
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

.schedule-panel {
  margin-top: 14px;
}

.schedule-month {
  color: #657487;
  font-size: 14px;
}

.schedule-layout {
  display: grid;
  grid-template-columns: minmax(320px, 1fr) minmax(280px, 420px);
  gap: 16px;
  margin-top: 14px;
}

.calendar-weekdays,
.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 6px;
}

.calendar-weekdays {
  margin-bottom: 8px;
  color: #657487;
  font-size: 13px;
  text-align: center;
}

.calendar-day {
  position: relative;
  aspect-ratio: 1 / 0.72;
  min-height: 46px;
  border: 1px solid #e3e8ef;
  border-radius: 8px;
  background: #fff;
  color: #223042;
  cursor: pointer;
  font: inherit;
}

.calendar-day:hover:not(.is-empty),
.calendar-day.is-selected {
  border-color: #1f5f8b;
  background: #eef7fc;
}

.calendar-day.is-today {
  box-shadow: inset 0 0 0 2px rgba(31, 95, 139, 0.18);
}

.calendar-day.is-empty {
  cursor: default;
  background: #f8fafc;
}

.schedule-dots {
  position: absolute;
  left: 50%;
  bottom: 7px;
  display: flex;
  gap: 4px;
  transform: translateX(-50%);
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.meeting-dot {
  background: #1f5f8b;
}

.activity-dot {
  background: #e6a23c;
}

.schedule-list {
  min-width: 0;
}

.schedule-list-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.schedule-list-title h4 {
  margin: 0;
  font-size: 16px;
  letter-spacing: 0;
}

.schedule-list-title span {
  color: #657487;
  white-space: nowrap;
}

.schedule-items {
  display: grid;
  gap: 10px;
}

.schedule-item {
  border: 1px solid #eef2f6;
  border-radius: 8px;
  padding: 10px;
}

.schedule-item div {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.schedule-item strong {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.schedule-item p {
  margin: 6px 0 0;
  color: #657487;
}

@media (max-width: 700px) {
  .panel-title,
  .announcement-row {
    flex-direction: column;
  }

  .schedule-layout {
    grid-template-columns: 1fr;
  }

  .calendar-day {
    min-height: 42px;
  }
}
</style>
