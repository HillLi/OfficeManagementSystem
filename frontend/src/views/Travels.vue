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
          <el-select v-model="form.staffLevel" placeholder="选择人员类别">
            <el-option label="一类人员" value="一类" />
            <el-option label="二类人员" value="二类" />
            <el-option label="三类人员" value="三类" />
          </el-select>
        </el-form-item>
        <el-form-item label="出差类型">
          <el-select v-model="form.travelType" placeholder="选择出差类型">
            <el-option label="教学科研业务" value="教学科研业务" />
            <el-option label="行政管理业务" value="行政管理业务" />
            <el-option label="学术交流" value="学术交流" />
          </el-select>
        </el-form-item>
        <el-form-item label="交通工具">
          <el-select v-model="form.transport" placeholder="选择交通工具">
            <el-option label="飞机" value="飞机" />
            <el-option label="高铁一等座" value="高铁一等座" />
            <el-option label="高铁二等座" value="高铁二等座" />
            <el-option label="火车软卧" value="火车软卧" />
            <el-option label="火车硬卧" value="火车硬卧" />
            <el-option label="火车硬座" value="火车硬座" />
          </el-select>
        </el-form-item>
        <el-form-item label="预算"><el-input-number v-model="form.budget" :min="0" /></el-form-item>
      </el-form>
      <el-button type="primary" @click="submit">提交差旅</el-button>
    </div>
    <div class="panel">
      <h3>差旅列表</h3>
      <el-table :data="rows" border>
        <el-table-column prop="destination" label="目的地" />
        <el-table-column prop="transport" label="交通工具" width="110" />
        <el-table-column prop="budget" label="预算" width="100" />
        <el-table-column label="标准" width="100"><template #default="{ row }">{{ row.checkResult?.standardAmount }}</template></el-table-column>
        <el-table-column label="超标"><template #default="{ row }">{{ row.checkResult?.exceeded ? '是' : '否' }}</template></el-table-column>
        <el-table-column prop="status" label="状态" width="130" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'

const rows = ref([])
const form = reactive({
  applicantId: 2, destination: '上海', startDate: '2026-06-01', endDate: '2026-06-03',
  reason: '参加高校信息化建设会议', staffLevel: '三类', travelType: '教学科研业务',
  transport: '高铁二等座', budget: 2600
})
const load = async () => { rows.value = await api.travels() }
const submit = async () => {
  try {
    await api.createTravel(form)
    ElMessage.success('已提交')
    load()
  } catch (e) {
    ElMessage.error(e.message || '提交失败')
  }
}
onMounted(load)
</script>
