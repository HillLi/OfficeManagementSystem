<template>
  <div class="org-user-tree-select">
    <el-tree
      ref="treeRef"
      :data="treeData"
      :props="treeProps"
      node-key="id"
      show-checkbox
      default-expand-all
      :check-strictly="true"
      @check="handleCheck"
    />
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

function flattenNodes(nodes = []) {
  return nodes.flatMap((node) => [
    node,
    ...flattenNodes(node.children || [])
  ])
}

const userNodes = computed(() => flattenNodes(props.treeData).filter((node) => node.type === 'user'))

const selectedUsers = computed(() => {
  const selected = new Set(props.modelValue.map((id) => String(id)))
  return userNodes.value.filter((node) => selected.has(String(node.userId)))
})

function checkedKeysFromModel() {
  return props.modelValue.map((id) => `user-${id}`)
}

function syncCheckedKeys() {
  nextTick(() => {
    treeRef.value?.setCheckedKeys(checkedKeysFromModel())
  })
}

function handleCheck() {
  const checkedNodes = treeRef.value?.getCheckedNodes(false, true) || []
  const userIds = checkedNodes
    .filter((node) => node.type === 'user')
    .map((node) => node.userId)
    .filter((userId) => userId !== undefined && userId !== null)
  emit('update:modelValue', Array.from(new Set(userIds)))
}

function removeUser(userId) {
  emit('update:modelValue', props.modelValue.filter((id) => String(id) !== String(userId)))
}

watch(
  () => [props.modelValue, props.treeData],
  syncCheckedKeys,
  { deep: true, immediate: true }
)
</script>

<style scoped>
.org-user-tree-select {
  display: grid;
  gap: 8px;
  min-height: 180px;
  max-height: 320px;
  overflow: auto;
  padding: 8px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background: #fff;
}

.selected-users {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding-top: 8px;
  border-top: 1px solid #ebeef5;
}
</style>
