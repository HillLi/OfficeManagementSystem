<template>
  <div class="approval-page">
    <h2 class="page-title">审批任务</h2>
    <el-tabs v-model="activeTab">
      <el-tab-pane v-for="tab in approvalTabs" :key="tab.name" :label="tab.label" :name="tab.name">
        <el-table v-if="tab.name === 'tasks'" :data="tasks" border>
          <el-table-column label="业务" width="100"><template #default="{ row }">{{ labelOf('biz_type', row.bizType) }}</template></el-table-column>
          <el-table-column prop="bizId" label="业务 ID" width="90" />
          <el-table-column label="当前节点" width="150"><template #default="{ row }">{{ labelOf('flow_node', row.nodeKey) }}</template></el-table-column>
          <el-table-column label="处理角色" width="140"><template #default="{ row }">{{ labelOf('role_key', row.approverRole) }}</template></el-table-column>
          <el-table-column label="发起人" width="104"><template #default="{ row }">{{ originatorNameOf(row, userOptions, instances) }}</template></el-table-column>
          <el-table-column prop="dueTime" label="截止时间" min-width="170" />
          <el-table-column label="办理" width="155">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button size="small" type="primary" @click="process(row, 'approve')">同意</el-button>
                <el-button size="small" type="danger" @click="process(row, 'reject')">退回</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <el-table v-else-if="tab.name === 'notifications'" :data="notifications" border>
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

        <el-table v-else-if="tab.name === 'instances'" :data="instances" border>
          <el-table-column label="业务" width="90"><template #default="{ row }">{{ labelOf('biz_type', row.bizType) }}</template></el-table-column>
          <el-table-column prop="bizId" label="ID" width="70" />
          <el-table-column label="节点" width="130"><template #default="{ row }">{{ labelOf('flow_node', row.currentNodeKey) }}</template></el-table-column>
          <el-table-column label="发起人" width="104"><template #default="{ row }">{{ originatorNameOf(row, userOptions) }}</template></el-table-column>
          <el-table-column label="状态"><template #default="{ row }">{{ labelOf('business_status', row.status) }}</template></el-table-column>
        </el-table>

        <el-table v-else :data="rows" border>
          <el-table-column label="业务类型" width="110"><template #default="{ row }">{{ labelOf('biz_type', row.bizType) }}</template></el-table-column>
          <el-table-column prop="bizId" label="业务 ID" width="90" />
          <el-table-column prop="operatorId" label="操作人" width="90" />
          <el-table-column label="发起人" width="104"><template #default="{ row }">{{ originatorNameOf(row, userOptions, instances) }}</template></el-table-column>
          <el-table-column prop="action" label="动作" width="100" />
          <el-table-column prop="opinion" label="意见" />
          <el-table-column prop="createdAt" label="时间" width="190" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import { useDictionaryStore } from '../stores/dictionary'
import { approvalTabs, defaultApprovalTab } from '../utils/approvalTabs'
import { originatorNameOf } from '../utils/userDisplay'

const dictionaryStore = useDictionaryStore()
const labelOf = dictionaryStore.labelOf
const activeTab = ref(defaultApprovalTab)
const rows = ref([])
const tasks = ref([])
const notifications = ref([])
const instances = ref([])
const userOptions = ref([])

const load = async () => {
  const [approvalRows, taskRows, notificationRows, instanceRows, users] = await Promise.all([
    api.approvals(),
    api.flowTasks({ onlyMine: true }),
    api.notifications({ unreadOnly: false }),
    api.flowInstances(),
    api.userOptions()
  ])
  rows.value = approvalRows
  tasks.value = taskRows
  notifications.value = notificationRows
  instances.value = instanceRows
  userOptions.value = users
}

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

const markRead = async (id) => {
  await api.markNotificationRead(id)
  await load()
}

onMounted(load)
</script>

<style scoped>
.page-title {
  margin-top: 0;
}
</style>
