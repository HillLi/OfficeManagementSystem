<template>
  <div class="user-manage">
    <div class="panel report-header">
      <h3>用户与部门管理</h3>
    </div>
    <div class="page-actions">
      <el-button v-if="activeTab === 'users'" type="primary" @click="openUserDialog(null)">新增用户</el-button>
      <el-button v-if="activeTab === 'depts'" type="primary" @click="openDeptDialog(null)">新增部门</el-button>
    </div>

    <el-tabs v-model="activeTab">
      <!-- 用户管理 Tab -->
      <el-tab-pane label="用户管理" name="users">
        <el-table v-loading="loading" :data="users" border stripe style="width:100%">
          <template #empty><el-empty description="暂无数据" /></template>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="realName" label="姓名" width="100" />
          <el-table-column prop="email" label="邮箱" min-width="170" />
          <el-table-column prop="deptName" label="部门" width="140" />
          <el-table-column label="角色" min-width="200">
            <template #default="{ row }">
              <el-tag v-for="r in row.roleKeys" :key="r" size="small" style="margin-right:4px">
                {{ roleLabel(r) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="openUserDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDeleteUser(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 部门管理 Tab -->
      <el-tab-pane label="部门管理" name="depts">
        <el-table v-loading="loading" :data="depts" border stripe style="width:100%">
          <template #empty><el-empty description="暂无数据" /></template>
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="deptName" label="部门名称" width="200" />
          <el-table-column label="上级部门" width="200">
            <template #default="{ row }">
              {{ deptNameMap[row.parentId] || '（顶级）' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160">
            <template #default="{ row }">
              <el-button size="small" @click="openDeptDialog(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDeleteDept(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 用户编辑/新增对话框 -->
    <el-dialog :title="userDialogTitle" v-model="userDialogVisible" width="520px" :close-on-click-modal="false">
      <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-width="80px">
        <el-form-item label="用户名" prop="username" v-if="!editingUser">
          <el-input v-model="userForm.username" placeholder="登录用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!editingUser">
          <el-input v-model="userForm.password" type="password" placeholder="登录密码" />
        </el-form-item>
        <el-form-item label="密码" v-else>
          <el-input v-model="userForm.password" type="password" placeholder="留空则不修改" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="userForm.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="用户邮箱" />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="userForm.deptId" clearable placeholder="选择部门" style="width:100%">
            <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-checkbox-group v-model="userForm.selectedRoles">
            <el-checkbox v-for="r in allRoles" :key="r" :label="r">
              {{ roleLabel(r) }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveUser">保存</el-button>
      </template>
    </el-dialog>

    <!-- 部门编辑/新增对话框 -->
    <el-dialog :title="deptDialogTitle" v-model="deptDialogVisible" width="420px" :close-on-click-modal="false">
      <el-form ref="deptFormRef" :model="deptForm" :rules="deptRules" label-width="80px">
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="deptForm.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="上级部门">
          <el-select v-model="deptForm.parentId" clearable placeholder="留空为顶级部门" style="width:100%">
            <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="d.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deptDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveDept">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<!-- 用户与部门管理页面：用户增删改查、部门增删改查、角色分配 -->
<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../api'
import { useDictionaryStore } from '../stores/dictionary'

const dictionaryStore = useDictionaryStore()
const activeTab = ref('users')
const users = ref([])
const depts = ref([])
const allRoles = ref([])
const saving = ref(false)
const loading = ref(false)
const userFormRef = ref(null)
const deptFormRef = ref(null)

// 获取角色显示名称
function roleLabel(key) {
  return dictionaryStore.labelOf('role_key', key)
}

// 构建部门ID到名称的映射
const deptNameMap = computed(() => {
  const m = { 0: '（顶级）' }
  depts.value.forEach(d => { m[d.id] = d.deptName })
  return m
})

// ---- 用户对话框 ----
const userDialogVisible = ref(false)
const editingUser = ref(null)
const userDialogTitle = computed(() => editingUser.value ? '编辑用户' : '新增用户')
const userForm = ref({
  username: '',
  password: '',
  realName: '',
  email: '',
  deptId: null,
  selectedRoles: []
})
// 用户表单校验规则（新增时用户名和密码必填）
const userRules = computed(() => {
  const base = {
    realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
    email: [
      { required: true, message: '请输入邮箱', trigger: 'blur' },
      { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
    ]
  }
  if (!editingUser.value) {
    base.username = [{ required: true, message: '请输入用户名', trigger: 'blur' }]
    base.password = [{ required: true, message: '请输入密码', trigger: 'blur' }]
  }
  return base
})
const deptRules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

// 打开用户新增/编辑对话框
function openUserDialog(user) {
  editingUser.value = user
  if (user) {
    userForm.value = {
      username: user.username,
      password: '',
      realName: user.realName,
      email: user.email,
      deptId: user.deptId,
      selectedRoles: [...(user.roleKeys || [])]
    }
  } else {
    userForm.value = {
      username: '',
      password: '',
      realName: '',
      email: '',
      deptId: null,
      selectedRoles: []
    }
  }
  userDialogVisible.value = true
}

// 保存用户（新增或更新）
async function handleSaveUser() {
  try {
    await userFormRef.value.validate()
    const f = userForm.value
    saving.value = true
    if (editingUser.value) {
      const payload = {
        realName: f.realName,
        email: f.email,
        deptId: f.deptId,
        roleKeys: f.selectedRoles.join(',')
      }
      if (f.password) payload.password = f.password
      await api.adminUpdateUser(editingUser.value.id, payload)
      ElMessage.success('用户更新成功')
    } else {
      await api.adminCreateUser({
        username: f.username,
        password: f.password,
        realName: f.realName,
        email: f.email,
        deptId: f.deptId,
        roleKeys: f.selectedRoles.join(',')
      })
      ElMessage.success('用户创建成功')
    }
    userDialogVisible.value = false
    await loadUsers()
  } catch (e) {
    if (e.message !== 'validation failed') {
      ElMessage.error(e.message || '保存失败')
    }
  } finally {
    saving.value = false
  }
}

// 删除用户（带确认提示）
async function handleDeleteUser(user) {
  try {
    await ElMessageBox.confirm(`确定删除用户「${user.realName}」？`, '确认', { type: 'warning' })
    await api.adminDeleteUser(user.id)
    ElMessage.success('删除成功')
    await loadUsers()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

// ---- 部门对话框 ----
const deptDialogVisible = ref(false)
const editingDept = ref(null)
const deptDialogTitle = computed(() => editingDept.value ? '编辑部门' : '新增部门')
const deptForm = ref({ deptName: '', parentId: null })

// 打开部门新增/编辑对话框
function openDeptDialog(dept) {
  editingDept.value = dept
  if (dept) {
    deptForm.value = { deptName: dept.deptName, parentId: dept.parentId || null }
  } else {
    deptForm.value = { deptName: '', parentId: null }
  }
  deptDialogVisible.value = true
}

// 保存部门（新增或更新）
async function handleSaveDept() {
  try {
    await deptFormRef.value.validate()
    const f = deptForm.value
    saving.value = true
    if (editingDept.value) {
      await api.adminUpdateDept(editingDept.value.id, f)
      ElMessage.success('部门更新成功')
    } else {
      await api.adminCreateDept(f)
      ElMessage.success('部门创建成功')
    }
    deptDialogVisible.value = false
    await loadDepts()
    await loadUsers()
  } catch (e) {
    if (e.message !== 'validation failed') {
      ElMessage.error(e.message || '保存失败')
    }
  } finally {
    saving.value = false
  }
}

// 删除部门（带确认提示）
async function handleDeleteDept(dept) {
  try {
    await ElMessageBox.confirm(`确定删除部门「${dept.deptName}」？`, '确认', { type: 'warning' })
    await api.adminDeleteDept(dept.id)
    ElMessage.success('删除成功')
    await loadDepts()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

// ---- 加载数据 ----

// 加载用户列表
async function loadUsers() {
  loading.value = true
  try {
    users.value = await api.adminUsers()
  } catch (e) {
    ElMessage.error(e.message || '加载用户失败')
  } finally {
    loading.value = false
  }
}

// 加载部门列表
async function loadDepts() {
  loading.value = true
  try {
    depts.value = await api.adminDepts()
  } catch (e) {
    ElMessage.error(e.message || '加载部门失败')
  } finally {
    loading.value = false
  }
}

// 加载所有角色列表
async function loadRoles() {
  try {
    allRoles.value = await api.adminRoles()
  } catch (e) {
    ElMessage.error(e.message || '加载角色失败')
  }
}

// 页面挂载时加载用户、部门和角色数据
onMounted(() => {
  loadUsers()
  loadDepts()
  loadRoles()
})
</script>
