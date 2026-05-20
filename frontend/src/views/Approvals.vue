<template>
  <div class="panel">
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
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../api'

const rows = ref([])
onMounted(async () => { rows.value = await api.approvals() })
</script>
