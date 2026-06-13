<template>
  <div class="travel-page">
    <div class="panel report-header">
      <h3>差旅审批</h3>
    </div>
    <div class="page-actions">
      <el-button type="primary" @click="applicationDialog = true">差旅申请</el-button>
    </div>

    <el-table :data="rows" border v-loading="loading">
      <template #empty><el-empty description="暂无差旅数据" /></template>
      <el-table-column prop="destination" label="目的地" />
      <el-table-column label="人员类别" width="92"><template #default="{ row }">{{ labelOf('staff_level', row.staffLevel) }}</template></el-table-column>
      <el-table-column label="出差类型" width="120"><template #default="{ row }">{{ labelOf('travel_type', row.travelType) }}</template></el-table-column>
      <el-table-column label="交通工具" width="110"><template #default="{ row }">{{ labelOf('transport_type', row.transport) }}</template></el-table-column>
      <el-table-column prop="budget" label="预算" width="92" />
      <el-table-column prop="actualExpense" label="实报" width="92" />
      <el-table-column label="标准" width="92"><template #default="{ row }">{{ row.checkResult?.standardAmount }}</template></el-table-column>
      <el-table-column label="超标" width="65"><template #default="{ row }">{{ row.checkResult?.exceeded ? '是' : '否' }}</template></el-table-column>
      <el-table-column label="状态" width="128"><template #default="{ row }">{{ labelOf('business_status', row.status) }}</template></el-table-column>
      <el-table-column label="办理" width="190">
        <template #default="{ row }">
          <div class="table-actions">
            <el-button v-if="row.status === 'approved'" size="small" type="success" @click="openReimburse(row)">报销登记</el-button>
            <el-button size="small" @click="openFlowGuide(row)">流程导览</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="applicationDialog" title="差旅申请" width="560px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="目的地" prop="destination"><el-input v-model="form.destination" /></el-form-item>
        <el-form-item label="出差事由" prop="reason"><el-input v-model="form.reason" /></el-form-item>
        <el-form-item label="出发日期" prop="startDate"><el-date-picker v-model="form.startDate" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="返回日期" prop="endDate"><el-date-picker v-model="form.endDate" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="人员类别">
          <el-select v-model="form.staffLevel"><el-option v-for="item in optionsOf('staff_level')" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        </el-form-item>
        <el-form-item label="出差类型">
          <el-select v-model="form.travelType"><el-option v-for="item in optionsOf('travel_type')" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        </el-form-item>
        <el-form-item label="交通工具">
          <el-select v-model="form.transport"><el-option v-for="item in optionsOf('transport_type')" :key="item.value" :label="item.label" :value="item.value" /></el-select>
        </el-form-item>
        <el-form-item label="预算"><el-input-number v-model="form.budget" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applicationDialog = false">取消</el-button>
        <el-button type="primary" @click="submit">提交差旅</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reimburseDialog" title="差旅报销登记" width="520px" :close-on-click-modal="false">
      <el-form ref="reimburseFormRef" :model="reimburseForm" :rules="reimburseRules" label-position="top">
        <el-form-item label="实际报销金额" prop="actualExpense"><el-input-number v-model="reimburseForm.actualExpense" :min="0" /></el-form-item>
        <el-form-item label="票据附件地址"><el-input v-model="reimburseForm.receiptUrl" /></el-form-item>
        <el-form-item label="超标准说明"><el-input v-model="reimburseForm.overLimitReason" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reimburseDialog = false">取消</el-button>
        <el-button type="primary" @click="reimburse">提交财务复核</el-button>
      </template>
    </el-dialog>
    <WorkflowGuideDialog ref="flowGuideDialog" />
  </div>
</template>

<!-- 差旅审批页面：差旅申请列表、提交差旅申请、报销登记 -->
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { useDictionaryStore } from '../stores/dictionary'
import { readSessionUser } from '../utils/sessionUser'
import WorkflowGuideDialog from '../components/WorkflowGuideDialog.vue'

const dictionaryStore = useDictionaryStore()
const labelOf = dictionaryStore.labelOf
const optionsOf = dictionaryStore.optionsOf
const currentUser = readSessionUser(undefined, { id: 2 })
const rows = ref([])
const applicationDialog = ref(false)
const reimburseDialog = ref(false)
const currentTravel = ref(null)
const flowGuideDialog = ref(null)
const formRef = ref(null)
const reimburseFormRef = ref(null)
const form = reactive({
  applicantId: currentUser.id,
  destination: '',
  startDate: '',
  endDate: '',
  reason: '',
  staffLevel: '',
  travelType: '',
  transport: '',
  budget: 0
})
const reimburseForm = reactive({ actualExpense: 0, receiptUrl: '', overLimitReason: '' })
const loading = ref(false)
const rules = {
  destination: [{ required: true, message: '请输入目的地', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入出差事由', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择出发日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择返回日期', trigger: 'change' }]
}
const reimburseRules = {
  actualExpense: [{ required: true, message: '请输入实际报销金额', trigger: 'blur' }]
}

// 加载差旅列表数据
const load = async () => {
  loading.value = true
  try {
    rows.value = await api.travels()
  } catch (e) {
    ElMessage.error(e.message || '加载差旅数据失败')
  } finally {
    loading.value = false
  }
}

// 提交差旅申请
const submit = async () => {
  try {
    await formRef.value.validate()
    await api.createTravel(form)
    ElMessage.success('差旅申请已提交')
    applicationDialog.value = false
    await load()
  } catch (error) {
    if (error.message !== 'validation failed') {
      ElMessage.error(error.message || '提交失败')
    }
  }
}

// 打开报销登记弹窗，回填已有数据
const openReimburse = (travel) => {
  currentTravel.value = travel
  reimburseForm.actualExpense = Number(travel.actualExpense || travel.budget || 0)
  reimburseForm.receiptUrl = travel.receiptUrl || ''
  reimburseForm.overLimitReason = travel.overLimitReason || ''
  reimburseDialog.value = true
}

// 提交报销申请（提交财务复核）
const reimburse = async () => {
  try {
    await reimburseFormRef.value.validate()
    await api.reimburseTravel(currentTravel.value.id, reimburseForm)
    ElMessage.success('报销申请已提交财务复核')
    reimburseDialog.value = false
    await load()
  } catch (error) {
    if (error.message !== 'validation failed') {
      ElMessage.error(error.message || '报销提交失败')
    }
  }
}

// 打开流程导览弹窗
const openFlowGuide = (travel) => {
  flowGuideDialog.value?.open('travel', travel.id)
}

// 页面挂载时加载差旅数据
onMounted(load)
</script>
