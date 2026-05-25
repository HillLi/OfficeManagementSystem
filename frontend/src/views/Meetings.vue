<template>
  <div class="page-grid">
    <div class="panel">
      <h3>会议申请</h3>
      <el-form label-position="top">
        <el-form-item label="主题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="会议室">
          <el-select v-model="form.roomId"><el-option v-for="room in rooms" :key="room.id" :label="`${room.roomName}（${room.capacity} 人）`" :value="room.id" /></el-select>
        </el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
        <el-form-item label="预计人数"><el-input-number v-model="form.expectedCount" :min="1" /></el-form-item>
        <el-form-item label="场地类型">
          <el-select v-model="form.venueType"><el-option label="室内" value="室内" /><el-option label="室外" value="室外" /></el-select>
        </el-form-item>
        <el-form-item label="会议类别">
          <el-select v-model="form.meetingType">
            <el-option label="国内管理会议" value="国内管理会议" />
            <el-option label="国内业务会议" value="国内业务会议" />
            <el-option label="在华举办的国际会议" value="在华举办的国际会议" />
          </el-select>
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
      <div class="toolbar">
        <el-button type="primary" @click="submit">提交会议</el-button>
        <el-tag v-if="isLarge" type="danger">大型活动</el-tag>
      </div>
      <p v-if="isLarge" class="rule-note">大型活动须至少提前 15 个工作日申请并提交安全材料。</p>
    </div>
    <div class="panel">
      <h3>会议列表</h3>
      <el-table :data="meetings" border>
        <el-table-column prop="title" label="主题" min-width="160" />
        <el-table-column prop="expectedCount" label="人数" width="65" />
        <el-table-column prop="budget" label="预算" width="90" />
        <el-table-column label="大型活动" width="88"><template #default="{ row }">{{ row.largeActivity ? '是' : '否' }}</template></el-table-column>
        <el-table-column prop="signInCount" label="签到" width="65" />
        <el-table-column prop="status" label="状态" width="115" />
        <el-table-column label="办理" width="106">
          <template #default="{ row }">
            <el-button v-if="row.status === 'approved'" size="small" type="success" @click="archiveMinutes(row)">纪要归档</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'

const currentUser = JSON.parse(sessionStorage.getItem('oms_user') || '{"id":2}')
const rooms = ref([])
const meetings = ref([])
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

onMounted(load)
</script>
