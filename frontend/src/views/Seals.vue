<template>
  <div class="page-grid">
    <div class="panel">
      <h3>用印申请</h3>
      <el-form label-position="top">
        <el-form-item label="印章"><el-select v-model="form.sealId"><el-option v-for="s in seals" :key="s.id" :label="s.sealName" :value="s.id" /></el-select></el-form-item>
        <el-form-item label="用途"><el-input v-model="form.purpose" /></el-form-item>
        <el-form-item label="材料URL"><el-input v-model="form.materialUrl" /></el-form-item>
        <el-form-item label="份数"><el-input-number v-model="form.copies" :min="1" /></el-form-item>
      </el-form>
      <el-button type="primary" @click="submit">提交申请</el-button>
    </div>
    <div class="panel">
      <h3>用印记录</h3>
      <el-table :data="apps" border>
        <el-table-column prop="sealId" label="印章ID" width="90" />
        <el-table-column prop="purpose" label="用途" />
        <el-table-column prop="copies" label="份数" width="80" />
        <el-table-column prop="status" label="状态" width="130" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'

const seals = ref([])
const apps = ref([])
const form = reactive({ sealId: 1, applicantId: 2, purpose: '系统试运行通知材料用印', materialUrl: '/files/demo.pdf', copies: 2 })
const load = async () => { seals.value = await api.seals(); apps.value = await api.sealApps() }
const submit = async () => { await api.createSealApp(form); ElMessage.success('已提交'); load() }
onMounted(load)
</script>
