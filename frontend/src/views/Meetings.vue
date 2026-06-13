<template>
  <div class="meeting-page">
    <div class="panel report-header">
      <h3>会议管理</h3>
    </div>

    <el-tabs v-model="activeTab">
      <!-- Tab 1: 会议列表 -->
      <el-tab-pane label="会议列表" name="list">
        <div class="page-actions">
          <el-button type="primary" @click="openApplicationDialog">会议申请</el-button>
        </div>

        <el-table :data="meetings" border v-loading="loading">
          <el-table-column prop="title" label="主题" min-width="160" />
          <el-table-column label="参会人" width="90" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click="showParticipants(row)">
                {{ row.expectedCount || 0 }} 人
              </el-button>
            </template>
          </el-table-column>
          <el-table-column label="类别" width="130">
            <template #default="{ row }">{{ labelOf('meeting_type', row.meetingType) }}</template>
          </el-table-column>
          <el-table-column label="场地" width="72">
            <template #default="{ row }">{{ labelOf('venue_type', row.venueType) }}</template>
          </el-table-column>
          <el-table-column prop="budget" label="预算" width="90" />
          <el-table-column label="大型活动" width="88">
            <template #default="{ row }">{{ row.largeActivity ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="115">
            <template #default="{ row }">{{ labelOf('business_status', row.status) }}</template>
          </el-table-column>
          <el-table-column label="办理" min-width="280">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button v-if="row.status === 'approved' && isRecorder(row)" size="small" type="success" @click="archiveMinutes(row)">填写纪要</el-button>
                <el-button v-if="row.status === 'minutes_confirmed' && isOrganizer(row)" size="small" type="warning" @click="publishMeeting(row)">发布为公告</el-button>
                <el-button v-if="row.status === 'minutes_confirmed' && isOrganizer(row)" size="small" @click="archiveDirectly(row)">直接归档</el-button>
                <el-button v-if="row.status === 'minutes_pending' || row.status === 'minutes_confirmed'" size="small" type="info" @click="showConfirmProgress(row)">确认进度</el-button>
                <el-button size="small" @click="openFlowGuide(row)">流程导览</el-button>
              </div>
            </template>
          </el-table-column>
          <template #empty><el-empty description="暂无会议" /></template>
        </el-table>
      </el-tab-pane>

      <!-- Tab 2: 我参与的会议 -->
      <el-tab-pane label="我参与的会议" name="participated">
        <el-table :data="participatedMeetings" border v-loading="loading">
          <el-table-column prop="title" label="主题" min-width="160" />
          <el-table-column label="开始时间" width="170">
            <template #default="{ row }">{{ formatDate(row.startTime) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="115">
            <template #default="{ row }">{{ labelOf('business_status', row.status) }}</template>
          </el-table-column>
          <el-table-column label="纪要状态" width="110">
            <template #default="{ row }">
              <el-tag v-if="row.status === 'minutes_pending'" type="warning">待确认</el-tag>
              <el-tag v-else-if="row.status === 'minutes_confirmed' || row.status === 'archived'" type="success">已确认</el-tag>
              <el-tag v-else type="info">未填写</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button v-if="row.status === 'minutes_pending' && !row.minutesConfirmed" size="small" type="primary" @click="confirmMinutes(row)">确认纪要</el-button>
              <el-button v-if="row.minutes" size="small" @click="viewMinutes(row)">查看纪要</el-button>
            </template>
          </el-table-column>
          <template #empty><el-empty description="暂无参与的会议" /></template>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- Application Dialog -->
    <el-dialog v-model="applicationDialog" title="会议申请" width="780px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <!-- 基本信息 -->
        <el-form-item label="主题" prop="title"><el-input v-model="form.title" placeholder="请输入会议主题" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="会议室" prop="roomId">
              <el-select v-model="form.roomId" placeholder="请选择会议室" style="width:100%">
                <el-option v-for="room in rooms" :key="room.id" :label="`${room.roomName}（${room.capacity} 人）`" :value="room.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
          </el-col>
        </el-row>

        <!-- 参会人员 -->
        <el-form-item :label="`参会人员（已选：${form.participants.length} 人）`" prop="participants">
          <OrgUserTreeSelect v-model="form.participants" :treeData="orgTree" />
        </el-form-item>
        <el-form-item label="记录员">
          <el-select v-model="form.recorderId" :disabled="form.participants.length === 0" clearable placeholder="请先选择参会人员" style="width:100%">
            <el-option v-for="uid in form.participants" :key="uid" :label="userName(uid)" :value="uid" />
          </el-select>
        </el-form-item>

        <!-- 会议类型 -->
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="场地类型">
              <el-select v-model="form.venueType" placeholder="请选择" style="width:100%"><el-option v-for="item in optionsOf('venue_type')" :key="item.value" :label="item.label" :value="item.value" /></el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="会议类别">
              <el-select v-model="form.meetingType" placeholder="请选择" style="width:100%"><el-option v-for="item in optionsOf('meeting_type')" :key="item.value" :label="item.label" :value="item.value" /></el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 费用明细 -->
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="住宿费"><el-input v-model.number="form.accommodationFee" type="number" :min="0" placeholder="0"><template #append>元</template></el-input></el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="伙食费"><el-input v-model.number="form.mealFee" type="number" :min="0" placeholder="0"><template #append>元</template></el-input></el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="场地费"><el-input v-model.number="form.venueFee" type="number" :min="0" placeholder="0"><template #append>元</template></el-input></el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="其他费用"><el-input v-model.number="form.otherFee" type="number" :min="0" placeholder="0"><template #append>元</template></el-input></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="申报预算"><el-input v-model.number="form.budget" type="number" :min="0" placeholder="0" style="width:200px"><template #append>元</template></el-input></el-form-item>

        <!-- 大型活动 -->
        <template v-if="isLarge">
          <el-divider content-position="left">大型活动材料</el-divider>
          <el-form-item label="风险报告地址"><el-input v-model="form.riskReportUrl" placeholder="请输入风险报告地址" /></el-form-item>
          <el-form-item label="安全方案地址"><el-input v-model="form.securityPlanUrl" placeholder="请输入安全方案地址" /></el-form-item>
          <el-form-item label="应急预案地址"><el-input v-model="form.emergencyPlanUrl" placeholder="请输入应急预案地址" /></el-form-item>
        </template>
      </el-form>
      <p v-if="isLarge" class="rule-note">大型活动须至少提前 15 个工作日申请并提交安全材料。</p>
      <template #footer>
        <el-tag v-if="isLarge" type="danger">大型活动</el-tag>
        <el-button @click="applicationDialog = false">取消</el-button>
        <el-button type="primary" @click="submit">提交会议</el-button>
      </template>
    </el-dialog>

    <!-- Participant Dialog -->
    <el-dialog v-model="participantDialog" :title="participantDialogTitle" width="640px" :close-on-click-modal="false">
      <el-table :data="participantList" border v-loading="participantLoading">
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column label="姓名" width="120">
          <template #default="{ row }">{{ userName(row.userId) }}</template>
        </el-table-column>
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.recorder" type="warning">记录员</el-tag>
            <el-tag v-else>参会人</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="确认状态" min-width="120">
          <template #default="{ row }">
            <el-tag v-if="row.minutesConfirmed" type="success">已确认</el-tag>
            <el-tag v-else type="info">未确认</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button v-if="!row.minutesConfirmed && participantDialogMode === 'progress'" size="small" link type="primary" @click="remindParticipant(row)">提醒</el-button>
          </template>
        </el-table-column>
        <template #empty><el-empty description="暂无参会人员" /></template>
      </el-table>
    </el-dialog>

    <!-- Minutes View Dialog -->
    <el-dialog v-model="minutesViewDialog" title="会议纪要" width="560px" :close-on-click-modal="false">
      <div style="white-space: pre-wrap; line-height: 1.8;">{{ minutesViewContent }}</div>
    </el-dialog>

    <WorkflowGuideDialog ref="flowGuideDialog" />
  </div>
</template>

<script setup>
// 会议管理页面：提供会议申请、纪要归档、纪要确认、发布为公告及参会人员管理等功能
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import { useDictionaryStore } from '../stores/dictionary'
import { readSessionUser } from '../utils/sessionUser'
import WorkflowGuideDialog from '../components/WorkflowGuideDialog.vue'
import OrgUserTreeSelect from '../components/OrgUserTreeSelect.vue'
import { formatDate } from '../utils/format'

const dictionaryStore = useDictionaryStore()
const labelOf = dictionaryStore.labelOf
const optionsOf = dictionaryStore.optionsOf
/** 当前登录用户 */
const currentUser = readSessionUser(undefined, { id: 2 })

const activeTab = ref('list')
const loading = ref(false)
const rooms = ref([])
const meetings = ref([])
const participatedMeetings = ref([])
const orgTree = ref([])
const userOptions = ref([])
const applicationDialog = ref(false)
const flowGuideDialog = ref(null)
const formRef = ref(null)

// Participant dialog state
const participantDialog = ref(false)
const participantDialogTitle = ref('参会人员')
const participantDialogMode = ref('view') // 'view' or 'progress'
const participantList = ref([])
const participantLoading = ref(false)

// Minutes view dialog state
const minutesViewDialog = ref(false)
const minutesViewContent = ref('')

const resetForm = () => ({
  title: '',
  roomId: null,
  organizerId: currentUser.id,
  startTime: '',
  endTime: '',
  participants: [],
  recorderId: null,
  venueType: '',
  meetingType: '',
  accommodationFee: 0,
  mealFee: 0,
  venueFee: 0,
  otherFee: 0,
  budget: 0,
  riskReportUrl: '',
  securityPlanUrl: '',
  emergencyPlanUrl: ''
})

const form = reactive(resetForm())

const rules = {
  title: [{ required: true, message: '请输入会议主题', trigger: 'blur' }],
  roomId: [{ required: true, message: '请选择会议室', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  participants: [{ required: true, type: 'array', message: '请选择参会人员', trigger: 'change' }]
}

/** 判断是否属于大型活动（室内>500人或室外>100人） */
const isLarge = computed(() =>
  (form.venueType === '室内' && form.participants.length > 500) ||
  (form.venueType === '室外' && form.participants.length > 100)
)

/** 根据用户ID获取用户姓名 */
function userName(uid) {
  const user = userOptions.value.find(u => u.id === uid || u.id === Number(uid))
  return user ? (user.realName || user.username || uid) : uid
}

/** 判断当前用户是否为该会议的记录员 */
function isRecorder(row) {
  return row.recorderId === currentUser.id
}

/** 判断当前用户是否为该会议的组织者 */
function isOrganizer(row) {
  return row.organizerId === currentUser.id
}

/** 加载会议室、会议列表、我参与的会议、组织树和用户列表数据 */
const load = async () => {
  loading.value = true
  try {
    const [roomsData, meetingsData, participatedData, treeData, userData] = await Promise.all([
      api.rooms(),
      api.meetings(),
      api.meetingsParticipated().catch(() => []),
      api.orgTree().catch(() => []),
      api.userOptions().catch(() => [])
    ])
    rooms.value = roomsData
    meetings.value = meetingsData
    participatedMeetings.value = participatedData
    orgTree.value = treeData
    userOptions.value = userData
  } catch (e) {
    ElMessage.error('加载数据失败：' + (e.message || '网络异常'))
  } finally {
    loading.value = false
  }
}

/** 打开会议申请弹窗并重置表单 */
const openApplicationDialog = () => {
  Object.assign(form, resetForm())
  applicationDialog.value = true
}

/** 提交会议申请 */
const submit = async () => {
  try {
    await formRef.value.validate()
    const data = { ...form }
    data.expectedCount = form.participants.length
    await api.createMeeting(data)
    ElMessage.success('会议申请已提交')
    applicationDialog.value = false
    await load()
  } catch (error) {
    if (error.message !== 'validation failed') {
      ElMessage.error(error.message || '提交失败')
    }
  }
}

/** 填写并归档会议纪要 */
const archiveMinutes = async (meeting) => {
  const { value } = await ElMessageBox.prompt('请输入会议纪要', '纪要归档', {
    inputType: 'textarea',
    inputValue: meeting.minutes || ''
  })
  await api.archiveMeetingMinutes(meeting.id, { minutes: value, signInCount: meeting.expectedCount })
  ElMessage.success('会议纪要已归档')
  await load()
}

/** 参会人确认会议纪要 */
const confirmMinutes = async (row) => {
  await ElMessageBox.confirm('确认会议纪要内容无误？', '确认纪要', { type: 'info' })
  await api.confirmMeetingMinutes(row.id)
  ElMessage.success('纪要已确认')
  await load()
}

/** 将会议发布为公告 */
const publishMeeting = async (row) => {
  await ElMessageBox.confirm('确定将该会议发布为公告？', '发布确认', { type: 'warning' })
  await api.publishMeeting(row.id)
  ElMessage.success('已发布为公告')
  await load()
}

/** 直接归档会议（不发布为公告） */
const archiveDirectly = async (row) => {
  await ElMessageBox.confirm('确定直接归档该会议？', '归档确认', { type: 'warning' })
  await api.archiveMeeting(row.id)
  ElMessage.success('会议已归档')
  await load()
}

/** 查看会议参会人员列表 */
const showParticipants = async (row) => {
  participantDialogMode.value = 'view'
  participantDialogTitle.value = `参会人员 — ${row.title}`
  participantDialog.value = true
  participantLoading.value = true
  try {
    participantList.value = await api.meetingParticipants(row.id)
  } catch (e) {
    ElMessage.error('加载参会人员失败')
    participantList.value = []
  } finally {
    participantLoading.value = false
  }
}

/** 查看参会人员的纪要确认进度 */
const showConfirmProgress = async (row) => {
  participantDialogMode.value = 'progress'
  participantDialogTitle.value = `确认进度 — ${row.title}`
  participantDialog.value = true
  participantLoading.value = true
  try {
    participantList.value = await api.meetingParticipants(row.id)
  } catch (e) {
    ElMessage.error('加载确认进度失败')
    participantList.value = []
  } finally {
    participantLoading.value = false
  }
}

/** 向未确认纪要的参会人发送提醒 */
const remindParticipant = async (participant) => {
  try {
    const meetingId = participantList.value.length > 0 ? participant.meetingId : null
    if (!meetingId) return
    await api.remindParticipant(meetingId, participant.userId)
    ElMessage.success('已发送提醒')
  } catch (e) {
    ElMessage.error('提醒失败：' + (e.message || '网络异常'))
  }
}

/** 查看会议纪要内容 */
const viewMinutes = (row) => {
  minutesViewContent.value = row.minutes || '暂无纪要内容'
  minutesViewDialog.value = true
}

/** 打开流程导览弹窗 */
const openFlowGuide = (meeting) => {
  flowGuideDialog.value?.open('meeting', meeting.id)
}

onMounted(load)
</script>

<style scoped>
.rule-note {
  color: #e6a23c;
  font-size: 12px;
}
</style>
