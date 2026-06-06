<template>
  <el-dialog v-model="visible" title="流程运行导览" width="min(980px, calc(100vw - 24px))" :close-on-click-modal="false">
    <el-skeleton v-if="loading" :rows="5" animated />
    <template v-else-if="guide">
      <div class="flow-guide-header">
        <div>
          <div class="flow-guide-title">{{ guide.title || `${guide.bizType} #${guide.bizId}` }}</div>
          <div class="flow-guide-subtitle">业务：{{ labelOf('biz_type', guide.bizType) }} #{{ guide.bizId }}，当前节点：{{ currentNodeText }}</div>
        </div>
        <el-tag :type="statusTagType(currentStep?.status || guide.status)">
          {{ currentStep ? currentStep.label : labelOf('business_status', guide.status) }}
        </el-tag>
      </div>

      <div class="flow-guide-track">
        <div v-for="(step, index) in guide.steps" :key="step.key" :class="stepClass(step)">
          <div class="flow-step-index">{{ index + 1 }}</div>
          <div class="flow-step-body">
            <div class="flow-step-label">{{ step.label }}</div>
            <el-tag size="small" :type="statusTagType(step.status)">{{ statusText(step.status) }}</el-tag>
          </div>
        </div>
      </div>

      <el-table :data="guide.steps" border class="flow-guide-table">
        <el-table-column prop="label" label="节点" min-width="150" />
        <el-table-column label="类型" width="96">
          <template #default="{ row }">{{ typeText(row.type) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理角色" min-width="120">
          <template #default="{ row }">{{ row.roleLabel || row.roleKey || '-' }}</template>
        </el-table-column>
        <el-table-column label="处理人" width="110">
          <template #default="{ row }">{{ row.operatorName || row.operatorId || '-' }}</template>
        </el-table-column>
        <el-table-column label="意见/说明" min-width="180">
          <template #default="{ row }">{{ row.opinion || '-' }}</template>
        </el-table-column>
        <el-table-column label="处理时间" width="170">
          <template #default="{ row }">{{ formatDate(row.time) }}</template>
        </el-table-column>
        <el-table-column label="截止时间" width="170">
          <template #default="{ row }">{{ formatDate(row.dueTime) }}</template>
        </el-table-column>
      </el-table>
    </template>
    <el-empty v-else description="暂无流程导览数据" />
  </el-dialog>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { useDictionaryStore } from '../stores/dictionary'
import { statusTagType, statusText, stepClass, typeText } from '../utils/workflowGuide'

const dictionaryStore = useDictionaryStore()
const labelOf = dictionaryStore.labelOf
const visible = ref(false)
const loading = ref(false)
const guide = ref(null)

const currentStep = computed(() => guide.value?.steps?.find((step) =>
  step.status === 'current' || step.status === 'rejected'
))
const currentNodeText = computed(() => {
  if (currentStep.value?.label) return currentStep.value.label
  const nodeKey = guide.value?.currentNodeKey
  const nodeLabel = labelOf('flow_node', nodeKey)
  if (nodeLabel && nodeLabel !== nodeKey) return nodeLabel
  return labelOf('business_status', guide.value?.status) || nodeKey || '-'
})

const open = async (bizType, bizId) => {
  visible.value = true
  loading.value = true
  guide.value = null
  try {
    guide.value = await api.workflowGuide({ bizType, bizId })
  } catch (error) {
    ElMessage.error(error.message || '流程导览加载失败')
  } finally {
    loading.value = false
  }
}

function formatDate(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '-'
}

defineExpose({ open })
</script>

<style scoped>
.flow-guide-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.flow-guide-title {
  color: #223042;
  font-size: 18px;
  font-weight: 700;
}

.flow-guide-subtitle {
  margin-top: 4px;
  color: #667085;
  font-size: 13px;
}

.flow-guide-track {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding: 4px 0 14px;
  margin-bottom: 10px;
}

.flow-step {
  min-width: 132px;
  display: grid;
  grid-template-columns: 28px 1fr;
  gap: 8px;
  align-items: start;
  padding: 10px;
  border: 1px solid #e3e8ef;
  border-radius: 10px;
  background: #f8fafc;
}

.flow-step-index {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #d9e4f2;
  color: #1f5f8b;
  font-weight: 700;
}

.flow-step-body {
  min-width: 0;
}

.flow-step-label {
  min-height: 36px;
  color: #223042;
  font-weight: 700;
  line-height: 1.35;
  margin-bottom: 8px;
}

.flow-step.is-done {
  border-color: #b7e2c0;
  background: #f0f9f2;
}

.flow-step.is-current {
  border-color: #f0c36d;
  background: #fff8eb;
}

.flow-step.is-rejected {
  border-color: #f3b5b5;
  background: #fff1f1;
}

.flow-step.is-optional {
  border-style: dashed;
}

.flow-guide-table {
  margin-top: 8px;
}
</style>
