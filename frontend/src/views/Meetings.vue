<template>
  <div class="meeting-page">
    <div class="panel report-header">
      <h3>会议管理</h3>
    </div>
    <div class="page-actions">
      <el-button type="primary" @click="applicationDialog = true">会议申请</el-button>
    </div>

    <el-table :data="meetings" border>
      <el-table-column prop="title" label="主题" min-width="160" />
      <el-table-column prop="expectedCount" label="人数" width="65" />
      <el-table-column label="类别" width="130"><template #default="{ row }">{{ labelOf('meeting_type', row.meetingType) }}</template></el-table-column>
      <el-table-column label="场地" width="72"><template #default="{ row }">{{ labelOf('venue_type', row.venueType) }}</template></el-table-column>
      <el-table-column prop="budget" label="预算" width="90" />
      <el-table-column label="大型活动" width="88"><template #default="{ row }">{{ row.largeActivity ? '是' : '否' }}</template></el-table-column>
      <el-table-column prop="signInCount" label="签到" width="65" />
      <el-table-column label="状态" width="115"><template #default="{ row }">{{ labelOf('business_status', row.status) }}</template></el-table-column>
      <el-table-column label="办理" width="190">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button v-if="row.status === 'approved'" size="small" type="success" @click="archiveMinutes(row)">纪要归档</el-button>
            <el-button size="small" @click="openFlowGuide(row)">流程导览</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="applicationDialog" title="会议申请" width="640px" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="主题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="会议室">
          <el-select v-model="form.roomId"><el-option v-for="room in rooms" :key="room.id" :label="`${room.roomName}（${room.capacity} 人）`" :value="room.id" /></el-select>
        </el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
        <el-form-item label="预计人数"><el-input-number v-model="form.expectedCount" :min="1" /></el-form-item>
        <el-form-item label="场地类型">
          <el-select v-model="form.venueType"><el-option v-for="item in optionsOf('venue_type')" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        </el-form-item>
        <el-form-item label="会议类别">
          <el-select v-model="form.meetingType"><el-option v-for="item in optionsOf('meeting_type')" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        </el-form-item>
        <el-form-item label="住宿费"><el-input-number v-model="form.accommodationFee" :min="0" /></el-form-item>
        <el-form-item label="伙食费"><el-input-number v-model="form.mealFee" :min="0" /></el-form-item>
        <el-form-item label="场地费"><el-input-number v-model="form.venueFee" :min="0" /></el-form-item>
        <el-form-item label="其他费用"><el-input-number v-model="form.otherFee" :min="0" /></el-form-item>
        <el-form-item label="申报预算"><el-input-number v-model="form.budget" :min="0" /></el-form-item>
        <template v-if="isLarge">
          <el-form-item label="风险报告地址"><el-input v-model="form.riskReportUrl" /></el-form-item>
          <el-form-item label="安全方案地址"><el-input v-model="form.securityPlanUrl" /></el-form-item>
          <el-form-item label="应急预案地址"><el-input v-model="form.emergencyPlanUrl" /></el-form-item>
        </template>
      </el-form>
      <p v-if="isLarge" class="rule-note">大型活动须至少提前 15 个工作日申请并提交安全材料。</p>
      <template #footer>
        <el-tag v-if="isLarge" type="danger">大型活动</el-tag>
        <el-button @click="applicationDialog = false">取消</el-button>
        <el-button type="primary" @click="submit">提交会议</el-button>
      </template>
    </el-dialog>
    <WorkflowGuideDialog ref="flowGuideDialog" />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import { useDictionaryStore } from '../stores/dictionary'
import WorkflowGuideDialog from '../components/WorkflowGuideDialog.vue'

const dictionaryStore = useDictionaryStore()
const labelOf = dictionaryStore.labelOf
const optionsOf = dictionaryStore.optionsOf
const currentUser = JSON.parse(sessionStorage.getItem('oms_user') || '{"id":2}')
const rooms = ref([])
const meetings = ref([])
const applicationDialog = ref(false)
const flowGuideDialog = ref(null)
const form = reactive({
  title: '系统试运行培训会',
  roomId: 1,
  organizerId: currentUser.id || 2,
  startTime: '2026-06-22T09:00:00',
  endTime: '2026-06-22T11:00:00',
  expectedCount: 60,
  venueType: '室内',
  meetingType: '国内管理会议',
  accommodationFee: 0,
  mealFee: 100,
  venueFee: 200,
  otherFee: 0,
  budget: 300,
  riskReportUrl: '',
  securityPlanUrl: '',
  emergencyPlanUrl: ''
})
const isLarge = computed(() =>
  (form.venueType === '室内' && form.expectedCount > 500) ||
  (form.venueType === '室外' && form.expectedCount > 100)
)

const load = async () => {
  rooms.value = await api.rooms()
  meetings.value = await api.meetings()
}
const submit = async () => {
  try {
    await api.createMeeting(form)
    ElMessage.success('会议申请已提交')
    applicationDialog.value = false
    await load()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  }
}
const archiveMinutes = async (meeting) => {
  const { value } = await ElMessageBox.prompt('请输入会议纪要', '纪要归档', {
    inputType: 'textarea',
    inputValue: meeting.minutes || ''
  })
  await api.archiveMeetingMinutes(meeting.id, { minutes: value, signInCount: meeting.expectedCount })
  ElMessage.success('会议纪要已归档')
  await load()
}
const openFlowGuide = (meeting) => {
  flowGuideDialog.value?.open('meeting', meeting.id)
}

onMounted(load)
</script>
