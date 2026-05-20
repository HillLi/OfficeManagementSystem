<template>
  <div class="page-grid">
    <div class="panel">
      <h3>会议申请</h3>
      <el-form label-position="top">
        <el-form-item label="主题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="会议室"><el-select v-model="form.roomId"><el-option v-for="r in rooms" :key="r.id" :label="`${r.roomName}（${r.capacity}人）`" :value="r.id" /></el-select></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
        <el-form-item label="人数"><el-input-number v-model="form.expectedCount" :min="1" /></el-form-item>
      </el-form>
      <el-button type="primary" @click="submit">提交会议</el-button>
    </div>
    <div class="panel">
      <h3>会议列表</h3>
      <el-table :data="meetings" border>
        <el-table-column prop="title" label="主题" />
        <el-table-column prop="expectedCount" label="人数" width="90" />
        <el-table-column label="大型活动" width="100"><template #default="{ row }">{{ row.largeActivity ? '是' : '否' }}</template></el-table-column>
        <el-table-column prop="status" label="状态" width="130" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'

const rooms = ref([])
const meetings = ref([])
const form = reactive({ title: '系统试运行培训会', roomId: 1, organizerId: 2, startTime: '2026-05-22T09:00:00', endTime: '2026-05-22T11:00:00', expectedCount: 60 })
const load = async () => { rooms.value = await api.rooms(); meetings.value = await api.meetings() }
const submit = async () => { await api.createMeeting(form); ElMessage.success('已提交'); load() }
onMounted(load)
</script>
