<template>
  <div class="workflow-page">
    <div class="panel report-header">
      <h3>审批流程配置</h3>
      <p class="tip">管理员可在此调整各业务的审批步骤顺序、节点名称和审批角色。修改后即时生效。</p>
    </div>

    <div class="panel flow-panel" v-loading="loading">
      <div class="flow-select-row">
        <span class="label">选择流程：</span>
        <el-select v-model="currentFlowKey" placeholder="请选择流程" style="width: 360px" @change="loadFlow">
          <el-option v-for="(name, key) in flowKeys" :key="key" :label="name" :value="key" />
        </el-select>
        <el-button v-if="currentFlowKey" type="primary" @click="addStep">新增步骤</el-button>
      </div>

      <el-empty v-if="!currentFlowKey" description="请选择一个流程" />
      <el-table v-else :data="editSteps" border stripe class="step-table">
        <template #empty><el-empty description="该流程暂无步骤，请新增" /></template>
        <el-table-column label="顺序" width="70" align="center">
          <template #default="{ $index }">{{ $index + 1 }}</template>
        </el-table-column>
        <el-table-column label="节点状态" min-width="180">
          <template #default="{ row }">
            <el-select v-model="row.nodeKey" placeholder="选择节点状态" style="width: 100%">
              <el-option v-for="item in nodeKeyOptions" :key="item.key" :label="item.label" :value="item.key" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="节点名称" min-width="180">
          <template #default="{ row }">
            <el-input v-model="row.nodeLabel" placeholder="如 部门负责人审批" />
          </template>
        </el-table-column>
        <el-table-column label="审批角色" min-width="180">
          <template #default="{ row }">
            <el-select v-model="row.roleKey" placeholder="选择角色" style="width: 100%">
              <el-option v-for="role in roles" :key="role.key" :label="role.label" :value="role.key" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ $index }">
            <el-button size="small" :disabled="$index === 0" @click="moveUp($index)">上移</el-button>
            <el-button size="small" :disabled="$index === editSteps.length - 1" @click="moveDown($index)">下移</el-button>
            <el-button size="small" type="danger" @click="removeStep($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="currentFlowKey" class="save-row">
        <el-button type="primary" :loading="saving" @click="save">保存配置</el-button>
        <span class="hint">保存后会覆盖该流程的所有步骤，正在审批中的历史单据不受影响</span>
      </div>
    </div>
  </div>
</template>

// 审批流程配置页：管理员配置各业务审批流程的步骤顺序、节点名称和审批角色
<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'

const loading = ref(false)
const saving = ref(false)
const flowKeys = ref({})
const currentFlowKey = ref('')
// 系统支持的审批角色集合
const roles = [
  { key: 'dept_head', label: '部门负责人' },
  { key: 'office_admin', label: '党办校办管理员' },
  { key: 'school_leader', label: '校级领导' },
  { key: 'security_staff', label: '保卫处人员' },
  { key: 'finance_staff', label: '财务处人员' }
]
// 系统支持的审批节点状态集合（下拉选项显示中文，存储英文Key）
const nodeKeyOptions = [
  { key: 'pending_dept', label: '部门负责人审批（pending_dept）' },
  { key: 'pending_office', label: '党办校办审核（pending_office）' },
  { key: 'pending_leader', label: '校级领导审批（pending_leader）' },
  { key: 'pending_security', label: '保卫处审批（pending_security）' },
  { key: 'pending_finance', label: '财务处审批（pending_finance）' },
  { key: 'pending_secret_review', label: '保密审查（pending_secret_review）' }
]
const editSteps = ref([])

// 页面加载时获取流程列表
onMounted(async () => {
  loading.value = true
  try {
    const [keys, grouped] = await Promise.all([api.adminWorkflowFlowKeys(), api.adminWorkflowNodes()])
    flowKeys.value = keys
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
})

// 加载选中流程的步骤
const loadFlow = async (flowKey) => {
  if (!flowKey) {
    editSteps.value = []
    return
  }
  loading.value = true
  try {
    const nodes = await api.adminWorkflowNodesByFlow(flowKey)
    editSteps.value = (nodes || []).map((n) => ({
      nodeKey: n.nodeKey,
      nodeLabel: n.nodeLabel,
      roleKey: n.roleKey
    }))
  } catch (e) {
    ElMessage.error(e.message || '加载流程失败')
  } finally {
    loading.value = false
  }
}

// 新增一个空白步骤
const addStep = () => {
  editSteps.value.push({ nodeKey: 'pending_dept', nodeLabel: '', roleKey: 'dept_head' })
}

// 删除步骤
const removeStep = (index) => {
  editSteps.value.splice(index, 1)
}

// 上移步骤
const moveUp = (index) => {
  if (index === 0) return
  const arr = editSteps.value
  const tmp = arr[index - 1]
  arr[index - 1] = arr[index]
  arr[index] = tmp
}

// 下移步骤
const moveDown = (index) => {
  const arr = editSteps.value
  if (index === arr.length - 1) return
  const tmp = arr[index + 1]
  arr[index + 1] = arr[index]
  arr[index] = tmp
}

// 保存流程配置
const save = async () => {
  if (editSteps.value.length === 0) {
    ElMessage.warning('请至少添加一个步骤')
    return
  }
  saving.value = true
  try {
    await api.adminSaveWorkflowFlow(currentFlowKey.value, editSteps.value)
    ElMessage.success('配置已保存并即时生效')
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.workflow-page {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.tip {
  margin: 6px 0 0;
  color: #657487;
  font-size: 13px;
}

.flow-panel {
  padding: 16px;
}

.flow-select-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.flow-select-row .label {
  font-weight: 600;
  white-space: nowrap;
}

.step-table {
  margin-bottom: 14px;
}

.save-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.save-row .hint {
  color: #909399;
  font-size: 13px;
}
</style>
