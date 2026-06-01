<template>
  <div class="announcement-page">
    <div class="announcement-header">
      <div>
        <h2>通知公告</h2>
        <p>查看面向当前用户范围发布的公告；管理员和党办校办人员可维护发布内容。</p>
      </div>
      <el-button v-if="canMaintain" type="primary" @click="openCreate">发布公告</el-button>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="公告列表" name="published">
        <el-empty v-if="publishedRows.length === 0" description="暂无可查看公告" />
        <div v-else class="announcement-list">
          <article
            v-for="row in publishedRows"
            :key="row.id"
            class="announcement-card"
            :class="{ pinned: row.pinned }"
          >
            <div class="card-title">
              <div>
                <el-tag v-if="row.pinned" type="danger" size="small">置顶</el-tag>
                <el-tag size="small">{{ categoryText(row.category) }}</el-tag>
                <h3>{{ row.title }}</h3>
              </div>
              <span class="time">{{ formatDate(row.publishedAt || row.updatedAt || row.createdAt) }}</span>
            </div>
            <p class="content-text">{{ row.content }}</p>
            <div class="scope-text">发布范围：{{ scopeText(row) }}</div>
          </article>
        </div>
      </el-tab-pane>

      <el-tab-pane v-if="canMaintain" label="公告维护" name="manage">
        <el-table :data="allRows" border stripe>
          <el-table-column prop="title" label="标题" min-width="220" />
          <el-table-column label="范围" min-width="120">
            <template #default="{ row }">{{ scopeText(row) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="置顶" width="72">
            <template #default="{ row }">{{ row.pinned ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column label="更新时间" width="180">
            <template #default="{ row }">{{ formatDate(row.updatedAt || row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="210">
            <template #default="{ row }">
              <el-button link @click="openEdit(row)">编辑</el-button>
              <el-button v-if="row.status !== 'published'" link type="primary" @click="publish(row)">发布</el-button>
              <el-button v-if="row.status === 'published'" link type="warning" @click="withdraw(row)">撤回</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑公告' : '新增公告'" width="620px">
      <el-form label-position="top">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category">
            <el-option label="通知" value="notice" />
            <el-option label="公告" value="announcement" />
            <el-option label="制度" value="policy" />
          </el-select>
        </el-form-item>
        <el-form-item label="发布范围">
          <el-radio-group v-model="form.targetType">
            <el-radio value="all">全校</el-radio>
            <el-radio value="dept">指定部门</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.targetType === 'dept'" label="部门编号">
          <el-input-number v-model="form.targetDeptId" :min="1" />
          <span class="form-tip">当前系统已有部门编号可在用户管理中查看。</span>
        </el-form-item>
        <el-form-item label="置顶"><el-switch v-model="form.pinned" /></el-form-item>
        <el-form-item label="正文"><el-input v-model="form.content" type="textarea" :rows="7" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const activeTab = ref('published')
const rows = ref([])
const allRows = ref([])
const dialogVisible = ref(false)
const editing = ref(null)
const saving = ref(false)
const form = reactive({
  title: '',
  content: '',
  category: 'notice',
  targetType: 'all',
  targetDeptId: null,
  pinned: false
})

const canMaintain = computed(() => userStore.roleKeys.includes('admin') || userStore.roleKeys.includes('office_admin'))
const publishedRows = computed(() => rows.value)

async function load() {
  rows.value = await api.announcements()
  if (canMaintain.value) {
    allRows.value = await api.announcements({ includeDrafts: true })
  }
}

function openCreate() {
  editing.value = null
  Object.assign(form, { title: '', content: '', category: 'notice', targetType: 'all', targetDeptId: null, pinned: false })
  dialogVisible.value = true
}

function openEdit(row) {
  editing.value = row
  Object.assign(form, {
    title: row.title,
    content: row.content,
    category: row.category || 'notice',
    targetType: row.targetType || 'all',
    targetDeptId: row.targetDeptId || null,
    pinned: Boolean(row.pinned)
  })
  dialogVisible.value = true
}

async function save() {
  saving.value = true
  try {
    if (editing.value) {
      await api.updateAnnouncement(editing.value.id, form)
    } else {
      await api.createAnnouncement(form)
    }
    dialogVisible.value = false
    await load()
    ElMessage.success('公告已保存')
  } finally {
    saving.value = false
  }
}

async function publish(row) {
  await api.publishAnnouncement(row.id)
  await load()
  ElMessage.success('公告已发布')
}

async function withdraw(row) {
  await api.withdrawAnnouncement(row.id)
  await load()
  ElMessage.success('公告已撤回')
}

function statusText(status) {
  return { draft: '草稿', published: '已发布', withdrawn: '已撤回' }[status] || status
}

function statusType(status) {
  return { draft: 'info', published: 'success', withdrawn: 'warning' }[status] || 'info'
}

function categoryText(category) {
  return { notice: '通知', announcement: '公告', policy: '制度' }[category] || '通知'
}

function scopeText(row) {
  return row.targetType === 'dept' ? `部门 #${row.targetDeptId}` : '全校'
}

function formatDate(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '-'
}

onMounted(load)
</script>

<style scoped>
.announcement-header,
.card-title {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.announcement-header {
  margin-bottom: 14px;
}

.announcement-header h2,
.announcement-card h3 {
  margin: 0;
}

.announcement-header p,
.scope-text,
.time,
.form-tip {
  color: #657487;
}

.announcement-header p {
  margin: 8px 0 0;
}

.announcement-list {
  display: grid;
  gap: 12px;
}

.announcement-card {
  background: #fff;
  border: 1px solid #e3e8ef;
  border-radius: 8px;
  padding: 16px;
}

.announcement-card.pinned {
  border-color: #f3b5b5;
  box-shadow: 0 8px 24px rgba(196, 86, 86, 0.08);
}

.card-title h3 {
  display: inline-block;
  margin-left: 8px;
}

.content-text {
  white-space: pre-wrap;
  line-height: 1.7;
}

.form-tip {
  margin-left: 10px;
  font-size: 13px;
}

@media (max-width: 700px) {
  .announcement-header,
  .card-title {
    flex-direction: column;
  }
}
</style>
