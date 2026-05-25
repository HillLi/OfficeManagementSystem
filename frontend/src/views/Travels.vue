<template>
  <div class="page-grid">
    <div class="panel">
      <h3>差旅申请</h3>
      <el-form label-position="top">
        <el-form-item label="目的地"><el-input v-model="form.destination" /></el-form-item>
        <el-form-item label="出差事由"><el-input v-model="form.reason" /></el-form-item>
        <el-form-item label="出发日期"><el-date-picker v-model="form.startDate" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="返回日期"><el-date-picker v-model="form.endDate" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="人员类别">
          <el-select v-model="form.staffLevel"><el-option label="一类人员" value="一类" /><el-option label="二类人员" value="二类" /><el-option label="三类人员" value="三类" /></el-select>
        </el-form-item>
        <el-form-item label="出差类型">
          <el-select v-model="form.travelType"><el-option label="教学科研业务" value="教学科研业务" /><el-option label="行政管理业务" value="行政管理业务" /><el-option label="学术交流" value="学术交流" /></el-select>
        </el-form-item>
        <el-form-item label="交通工具">
          <el-select v-model="form.transport">
            <el-option label="飞机" value="飞机" /><el-option label="高铁一等座" value="高铁一等座" /><el-option label="高铁二等座" value="高铁二等座" />
            <el-option label="火车软卧" value="火车软卧" /><el-option label="火车硬卧" value="火车硬卧" /><el-option label="火车硬座" value="火车硬座" />
          </el-select>
        </el-form-item>
        <el-form-item label="预算"><el-input-number v-model="form.budget" :min="0" /></el-form-item>
      </el-form>
      <el-button type="primary" @click="submit">提交差旅</el-button>
    </div>
    <div class="panel">
      <h3>差旅办理列表</h3>
      <el-table :data="rows" border>
        <el-table-column prop="destination" label="目的地" />
        <el-table-column prop="transport" label="交通工具" width="110" />
        <el-table-column prop="budget" label="预算" width="92" />
        <el-table-column prop="actualExpense" label="实报" width="92" />
        <el-table-column label="标准" width="92"><template #default="{ row }">{{ row.checkResult?.standardAmount }}</template></el-table-column>
        <el-table-column label="超标" width="65"><template #default="{ row }">{{ row.checkResult?.exceeded ? '是' : '否' }}</template></el-table-column>
        <el-table-column prop="status" label="状态" width="128" />
        <el-table-column label="办理" width="106">
          <template #default="{ row }">
            <el-button v-if="row.status === 'approved'" size="small" type="success" @click="openReimburse(row)">报销登记</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="reimburseDialog" title="差旅报销登记" width="520px">
      <el-form label-position="top">
        <el-form-item label="实际报销金额"><el-input-number v-model="reimburseForm.actualExpense" :min="0" /></el-form-item>
        <el-form-item label="票据附件地址"><el-input v-model="reimburseForm.receiptUrl" /></el-form-item>
        <el-form-item label="超标准说明"><el-input v-model="reimburseForm.overLimitReason" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reimburseDialog = false">取消</el-button>
        <el-button type="primary" @click="reimburse">提交财务复核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'

const currentUser = JSON.parse(sessionStorage.getItem('oms_user') || '{"id":2}')
const rows = ref([])
const reimburseDialog = ref(false)
const currentTravel = ref(null)
const form = reactive({
  applicantId: currentUser.id || 2,
  destination: '上海',
  startDate: '2026-06-01',
  endDate: '2026-06-03',
  reason: '参加高校信息化建设会议',
  staffLevel: '三类',
  travelType: '教学科研业务',
  transport: '高铁二等座',
  budget: 2600
})
const reimburseForm = reactive({ actualExpense: 0, receiptUrl: '', overLimitReason: '' })

const load = async () => { rows.value = await api.travels() }
const submit = async () => {
  try {
    await api.createTravel(form)
    ElMessage.success('差旅申请已提交')
    await load()
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  }
}
const openReimburse = (travel) => {
  currentTravel.value = travel
  reimburseForm.actualExpense = Number(travel.actualExpense || travel.budget || 0)
  reimburseForm.receiptUrl = travel.receiptUrl || ''
  reimburseForm.overLimitReason = travel.overLimitReason || ''
  reimburseDialog.value = true
}
const reimburse = async () => {
  try {
    await api.reimburseTravel(currentTravel.value.id, reimburseForm)
    ElMessage.success('报销申请已提交财务复核')
    reimburseDialog.value = false
    await load()
  } catch (error) {
    ElMessage.error(error.message || '报销提交失败')
  }
}

onMounted(load)
</script>
