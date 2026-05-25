<template>
  <div class="page-grid">
    <div class="panel">
      <h3>用印申请</h3>
      <el-form label-position="top">
        <el-form-item label="印章">
          <el-select v-model="form.sealId"><el-option v-for="seal in seals" :key="seal.id" :label="seal.sealName" :value="seal.id" /></el-select>
        </el-form-item>
        <el-form-item label="用途"><el-input v-model="form.purpose" /></el-form-item>
        <el-form-item label="材料地址"><el-input v-model="form.materialUrl" /></el-form-item>
        <el-form-item label="份数"><el-input-number v-model="form.copies" :min="1" /></el-form-item>
        <el-form-item label="事项等级">
          <el-select v-model="form.matterLevel">
            <el-option v-for="level in matterLevels" :key="level" :value="level" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否外带">
          <el-switch v-model="form.takeOut" active-text="外带" inactive-text="在馆使用" />
        </el-form-item>
        <template v-if="form.takeOut">
          <el-form-item label="外带原因"><el-input v-model="form.takeOutReason" /></el-form-item>
          <el-form-item label="使用地点"><el-input v-model="form.takeOutLocation" /></el-form-item>
          <el-form-item label="监督人 ID"><el-input-number v-model="form.supervisorId" :min="1" /></el-form-item>
          <el-form-item label="预计归还时间">
            <el-date-picker v-model="form.expectedReturnTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
          </el-form-item>
        </template>
      </el-form>
      <el-button type="primary" @click="submit">提交申请</el-button>
      <p v-if="form.takeOut" class="rule-note">外带用印须登记监督人、地点及预计归还时间。</p>
    </div>
    <div class="panel">
      <h3>用印办理记录</h3>
      <el-table :data="apps" border>
        <el-table-column prop="sealId" label="印章" width="65" />
        <el-table-column prop="purpose" label="用途" min-width="150" />
        <el-table-column prop="matterLevel" label="事项等级" width="100" />
        <el-table-column label="外带" width="65"><template #default="{ row }">{{ row.takeOut ? '是' : '否' }}</template></el-table-column>
        <el-table-column prop="returnDeadline" label="归还截止" min-width="165" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column v-if="canManage" label="办理" width="145">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button v-if="row.status === 'approved'" size="small" type="primary" @click="markUsed(row.id)">登记用印</el-button>
              <el-button v-if="row.status === 'used'" size="small" type="success" @click="markReturned(row.id)">确认归还</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div v-if="canManage" class="panel">
      <h3>印章移交登记</h3>
      <el-form label-position="top">
        <el-form-item label="印章"><el-select v-model="transferForm.sealId"><el-option v-for="seal in seals" :key="seal.id" :label="seal.sealName" :value="seal.id" /></el-select></el-form-item>
        <el-form-item label="接收人 ID"><el-input-number v-model="transferForm.receiverId" :min="1" /></el-form-item>
        <el-form-item label="监督人 ID"><el-input-number v-model="transferForm.supervisorId" :min="1" /></el-form-item>
        <el-form-item label="移交材料地址"><el-input v-model="transferForm.materialUrl" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="transferForm.remark" /></el-form-item>
      </el-form>
      <el-button type="primary" @click="createTransfer">登记移交</el-button>
    </div>
    <div v-if="canManage" class="panel">
      <h3>移交记录</h3>
      <el-table :data="transfers" border>
        <el-table-column prop="sealId" label="印章" width="65" />
        <el-table-column prop="transferorId" label="移交人" width="75" />
        <el-table-column prop="receiverId" label="接收人" width="75" />
        <el-table-column prop="supervisorId" label="监督人" width="75" />
        <el-table-column prop="materialUrl" label="材料地址" />
        <el-table-column prop="transferTime" label="移交时间" width="175" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'

const currentUser = JSON.parse(sessionStorage.getItem('oms_user') || '{"id":0,"roleKeys":[]}')
const canManage = computed(() => currentUser.roleKeys?.some((role) => ['seal_keeper', 'office_admin', 'admin'].includes(role)))
const seals = ref([])
const apps = ref([])
const transfers = ref([])
const matterLevels = ['常规事项', '一般事项', '重大事项']
const form = reactive({
  sealId: 1,
  applicantId: currentUser.id || 2,
  purpose: '系统试运行通知材料用印',
  materialUrl: '/files/seal-application.pdf',
  copies: 2,
  takeOut: false,
  matterLevel: '常规事项',
  takeOutReason: '',
  takeOutLocation: '',
  supervisorId: null,
  expectedReturnTime: ''
})
const transferForm = reactive({
  sealId: 1,
  receiverId: 2,
  supervisorId: currentUser.id || 2,
  materialUrl: '/files/seal-transfer.pdf',
  remark: ''
})

const load = async () => {
  seals.value = await api.seals()
  apps.value = await api.sealApps()
  if (canManage.value) {
    transfers.value = await api.sealTransfers()
  }
}
const submit = async () => {
  try {
    await api.createSealApp(form)
    ElMessage.success('用印申请已提交')
    await load()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  }
}
const markUsed = async (id) => {
  await api.markSealUsed(id, currentUser.id)
  ElMessage.success('用印状态已登记')
  await load()
}
const markReturned = async (id) => {
  await api.returnSeal(id, currentUser.id)
  ElMessage.success('归还状态已确认')
  await load()
}
const createTransfer = async () => {
  await api.createSealTransfer(transferForm)
  ElMessage.success('印章移交已登记')
  await load()
}

onMounted(load)
</script>
