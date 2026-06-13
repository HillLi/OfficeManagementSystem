<template>
  <div class="org-user-tree-select">
    <div class="tree-area">
      <el-tree
        ref="treeRef"
        :data="treeData"
        :props="treeProps"
        node-key="id"
        show-checkbox
        default-expand-all
        :check-strictly="false"
        @check="handleCheck"
      />
    </div>
    <div v-if="selectedUsers.length" class="selected-users">
      <el-tag
        v-for="user in selectedUsers"
        :key="user.id"
        size="small"
        closable
        @close="removeUser(user.userId)"
      >
        {{ user.label }}
      </el-tag>
    </div>
  </div>
</template>

<!-- 组织架构-用户树形选择器组件：以树形结构展示部门和人员，支持勾选用户 -->
<script setup>
import { computed, nextTick, ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  treeData: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const treeRef = ref()
const treeProps = {
  label: 'label',
  children: 'children'
}

// 递归展平树节点为一维数组
function flattenNodes(nodes = []) {
  return nodes.flatMap((node) => [
    node,
    ...flattenNodes(node.children || [])
  ])
}

// 从树数据中筛选出所有用户节点
const userNodes = computed(() => flattenNodes(props.treeData).filter((node) => node.type === 'user'))

// 根据已选ID计算已选用户信息列表（用于底部标签展示）
const selectedUsers = computed(() => {
  const selected = new Set(props.modelValue.map((id) => String(id)))
  return userNodes.value.filter((node) => selected.has(String(node.userId)))
})

// 将已选用户ID转为树组件所需的选中key
function checkedKeysFromModel() {
  return props.modelValue.map((id) => `user-${id}`)
}

// 同步树组件的勾选状态与外部modelValue
function syncCheckedKeys() {
  nextTick(() => {
    treeRef.value?.setCheckedKeys(checkedKeysFromModel())
  })
}

// 树节点勾选变化时，提取所有已选用户ID并向上传递
function handleCheck() {
  const checkedNodes = treeRef.value?.getCheckedNodes(false, true) || []
  const userIds = checkedNodes
    .filter((node) => node.type === 'user')
    .map((node) => node.userId)
    .filter((userId) => userId !== undefined && userId !== null)
  emit('update:modelValue', Array.from(new Set(userIds)))
}

// 移除指定用户（点击标签关闭按钮）
function removeUser(userId) {
  emit('update:modelValue', props.modelValue.filter((id) => String(id) !== String(userId)))
}

// 监听modelValue和treeData变化，同步树的勾选状态
watch(
  () => [props.modelValue, props.treeData],
  syncCheckedKeys,
  { deep: true, immediate: true }
)
</script>

<style scoped>
.org-user-tree-select {
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #fff;
  width: 100%;
}

.org-user-tree-select :deep(.el-tree) {
  padding: 8px 6px;
}

.tree-area {
  height: 220px;
  overflow-y: auto;
  overflow-x: hidden;
}

.selected-users {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 8px 10px;
  max-height: 80px;
  overflow-y: auto;
  border-top: 1px solid #ebeef5;
}
</style>
