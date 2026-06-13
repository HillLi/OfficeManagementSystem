<template>
  <div class="approval-page">
    <div class="panel report-header">
      <h3>审批任务</h3>
    </div>
    <el-tabs v-model="activeTab">
      <el-tab-pane v-for="tab in approvalTabs" :key="tab.name" :name="tab.name">
        <template #label>
          <span class="approval-tab-label">
            {{ tab.label }}
            <el-badge v-if="tabBadge(tab.name) > 0" :value="tabBadge(tab.name)" class="approval-tab-badge" />
          </span>
        </template>
        <el-table v-if="tab.name === 'tasks'" :data="tasks" border v-loading="loading">
          <template #empty><el-empty description="暂无待办任务" /></template>
          <el-table-column label="业务" width="100"><template #default="{ row }">{{ labelOf('biz_type', row.bizType) }}</template></el-table-column>
          <el-table-column prop="bizId" label="业务 ID" width="90" />
          <el-table-column label="当前节点" width="150"><template #default="{ row }">{{ labelOf('flow_node', row.nodeKey) }}</template></el-table-column>
          <el-table-column label="处理角色" width="140"><template #default="{ row }">{{ labelOf('role_key', row.approverRole) }}</template></el-table-column>
          <el-table-column label="截止时间" min-width="170">
            <template #default="{ row }">{{ formatDate(row.dueTime) }}</template>
          </el-table-column>
          <el-table-column label="办理" width="230">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button size="small" type="primary" @click="process(row, 'approve')">同意</el-button>
                <el-button size="small" type="danger" @click="process(row, 'reject')">退回</el-button>
                <el-button size="small" @click="openFlowGuide(row.bizType, row.bizId)">导览</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <el-table v-else-if="tab.name === 'notifications'" :data="notifications" border v-loading="loading">
          <template #empty><el-empty description="暂无通知" /></template>
          <el-table-column prop="title" label="标题" width="150" />
          <el-table-column prop="content" label="内容" />
          <el-table-column label="状态" width="70">
            <template #default="{ row }">{{ row.readStatus ? '已读' : '未读' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="76">
            <template #default="{ row }">
              <el-button v-if="!row.readStatus" size="small" @click="markRead(row.id)">已读</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-table v-else-if="tab.name === 'instances'" :data="instances" border v-loading="loading">
          <template #empty><el-empty description="暂无流程实例" /></template>
          <el-table-column label="业务" width="90"><template #default="{ row }">{{ labelOf('biz_type', row.bizType) }}</template></el-table-column>
          <el-table-column prop="bizId" label="ID" width="70" />
          <el-table-column label="节点" width="130"><template #default="{ row }">{{ labelOf('flow_node', row.currentNodeKey) }}</template></el-table-column>
          <el-table-column label="状态"><template #default="{ row }">{{ labelOf('business_status', row.status) }}</template></el-table-column>
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-button size="small" @click="openFlowGuide(row.bizType, row.bizId)">导览</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-table v-else :data="rows" border v-loading="loading">
          <template #empty><el-empty description="暂无审批记录" /></template>
          <el-table-column label="业务类型" width="110"><template #default="{ row }">{{ labelOf('biz_type', row.bizType) }}</template></el-table-column>
          <el-table-column prop="bizId" label="业务 ID" width="90" />
          <el-table-column prop="operatorId" label="操作人" width="90" />
          <el-table-column prop="action" label="动作" width="100" />
          <el-table-column prop="opinion" label="意见" />
          <el-table-column label="时间" width="190">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-button size="small" @click="openFlowGuide(row.bizType, row.bizId)">导览</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
    <WorkflowGuideDialog ref="flowGuideDialog" />
  </div>
</template>

// 审批任务管理页面：展示待办任务、通知消息、流程实例和审批记录，支持审批操作
<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import { useDictionaryStore } from '../stores/dictionary'
import { useActionBadgeStore } from '../stores/actionBadges'
import { approvalTabs, defaultApprovalTab } from '../utils/approvalTabs'
import { approvalTabBadgeCount } from '../utils/actionBadges'
import WorkflowGuideDialog from '../components/WorkflowGuideDialog.vue'
import { formatDate } from '../utils/format'

const dictionaryStore = useDictionaryStore()
const labelOf = dictionaryStore.labelOf
const actionBadgeStore = useActionBadgeStore()
const activeTab = ref(defaultApprovalTab)
const rows = ref([])
const tasks = ref([])
const notifications = ref([])
const instances = ref([])
const flowGuideDialog = ref(null)
const loading = ref(false)

// 加载审批相关数据（审批记录、待办任务、通知、流程实例）
const load = async () => {
  loading.value = true
  try {
    rows.value = await api.approvals()
    tasks.value = await api.flowTasks({ onlyMine: true })
    notifications.value = await api.notifications({ unreadOnly: false })
    instances.value = await api.flowInstances()
    actionBadgeStore.setFromData(tasks.value, notifications.value)
  } catch (e) {
    ElMessage.error(e.message || '加载审批数据失败')
  } finally {
    loading.value = false
  }
}

// 获取标签页的角标数量
const tabBadge = (tabName) => approvalTabBadgeCount(tabName, {
  pendingTasks: actionBadgeStore.pendingTasks,
  unreadNotifications: actionBadgeStore.unreadNotifications
})

// 处理审批任务：同意或退回，弹出意见输入框后提交
const process = async (row, action) => {
  try {
    const label = action === 'approve' ? '审批同意' : '退回申请'
    const { value } = await ElMessageBox.prompt('请输入处理意见', label, {
      inputValue: action === 'approve' ? '同意，按流程继续办理。' : '请补充材料后重新提交。',
      inputType: 'textarea'
    })
    await api.approve(row.bizType, row.bizId, { action, opinion: value })
    ElMessage.success(`${label}完成`)
    await load()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.message || '办理失败')
    }
  }
}

// 标记通知为已读
const markRead = async (id) => {
  try {
    await api.markNotificationRead(id)
    await load()
  } catch (e) {
    ElMessage.error(e.message || '标记失败')
  }
}

// 打开流程导览弹窗
const openFlowGuide = (bizType, bizId) => {
  flowGuideDialog.value?.open(bizType, bizId)
}

// 页面挂载时加载审批数据
onMounted(load)
</script>

<style scoped>
.approval-tab-label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.approval-tab-badge {
  line-height: 1;
}
</style>
