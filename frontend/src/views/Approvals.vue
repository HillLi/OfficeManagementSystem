<template>
  <div class="page-grid">
    <div class="panel">
      <h3>待办任务</h3>
      <el-table :data="tasks" border>
        <el-table-column prop="bizType" label="业务" width="90" />
        <el-table-column prop="bizId" label="ID" width="80" />
        <el-table-column prop="nodeKey" label="节点" width="130" />
        <el-table-column prop="approverRole" label="处理角色" width="120" />
        <el-table-column prop="dueTime" label="截止时间" />
      </el-table>
    </div>
    <div class="panel">
      <h3>通知提醒</h3>
      <el-table :data="notifications" border>
        <el-table-column prop="title" label="标题" width="140" />
        <el-table-column prop="content" label="内容" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">{{ row.readStatus ? '已读' : '未读' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90">
          <template #default="{ row }">
            <el-button v-if="!row.readStatus" size="small" @click="markRead(row.id)">已读</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="panel wide">
      <h3>流程实例</h3>
      <el-table :data="instances" border>
        <el-table-column prop="bizType" label="业务类型" width="110" />
        <el-table-column prop="bizId" label="业务ID" width="90" />
        <el-table-column prop="currentNodeKey" label="当前节点" width="140" />
        <el-table-column prop="status" label="流程状态" width="120" />
        <el-table-column prop="updatedAt" label="更新时间" width="190" />
      </el-table>
    </div>
    <div class="panel wide">
      <h3>审批记录</h3>
      <el-table :data="rows" border>
        <el-table-column prop="bizType" label="业务类型" width="110" />
        <el-table-column prop="bizId" label="业务ID" width="90" />
        <el-table-column prop="operatorId" label="操作人" width="90" />
        <el-table-column prop="action" label="动作" width="100" />
        <el-table-column prop="opinion" label="意见" />
        <el-table-column prop="createdAt" label="时间" width="190" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../api'

const rows = ref([])
const tasks = ref([])
const notifications = ref([])
const instances = ref([])
const load = async () => {
  rows.value = await api.approvals()
  tasks.value = await api.flowTasks({ onlyMine: true })
  notifications.value = await api.notifications({ unreadOnly: false })
  instances.value = await api.flowInstances()
}
const markRead = async (id) => {
  await api.markNotificationRead(id)
  load()
}
onMounted(load)
</script>
