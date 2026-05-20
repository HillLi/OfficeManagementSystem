<template>
  <div class="page-grid">
    <div class="panel">
      <h3>用印申请</h3>
      <el-form label-position="top">
        <el-form-item label="印章"><el-select v-model="form.sealId"><el-option v-for="s in seals" :key="s.id" :label="s.sealName" :value="s.id" /></el-select></el-form-item>
        <el-form-item label="用途"><el-input v-model="form.purpose" /></el-form-item>
        <el-form-item label="材料URL"><el-input v-model="form.materialUrl" placeholder="必填，严禁在空白纸张上用印" /></el-form-item>
        <el-form-item label="份数"><el-input-number v-model="form.copies" :min="1" /></el-form-item>
        <el-form-item label="是否外带">
          <el-switch v-model="form.takeOut" active-text="外带" inactive-text="在馆" />
        </el-form-item>
        <el-form-item label="事项等级">
          <el-select v-model="form.matterLevel">
            <el-option label="常规事项" value="常规事项" />
            <el-option label="一般事项" value="一般事项" />
            <el-option label="重大事项" value="重大事项" />
          </el-select>
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="submit">提交申请</el-button>
      <p v-if="form.takeOut" style="color:#e6a23c;margin-top:8px">外带申请需在7天内归还</p>
    </div>
    <div class="panel">
      <h3>用印记录</h3>
      <el-table :data="apps" border>
        <el-table-column prop="sealId" label="印章ID" width="80" />
        <el-table-column prop="purpose" label="用途" />
        <el-table-column prop="copies" label="份数" width="70" />
        <el-table-column label="外带" width="70"><template #default="{ row }">{{ row.takeOut ? '是' : '否' }}</template></el-table-column>
        <el-table-column label="归还截止"><template #default="{ row }">{{ row.returnDeadline || '-' }}</template></el-table-column>
        <el-table-column prop="status" label="状态" width="120" />
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
const form = reactive({
  sealId: 1, applicantId: 2, purpose: '系统试运行通知材料用印',
  materialUrl: '/files/demo.pdf', copies: 2, takeOut: false, matterLevel: '常规事项'
})
const load = async () => { seals.value = await api.seals(); apps.value = await api.sealApps() }
const submit = async () => {
  try {
    await api.createSealApp(form)
    ElMessage.success('已提交')
    load()
  } catch (e) {
    ElMessage.error(e.message || '提交失败')
  }
}
onMounted(load)
</script>
