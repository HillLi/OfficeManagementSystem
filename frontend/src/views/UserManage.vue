<template>
  <div class="user-manage">
    <h2 style="margin-top:0">用户与部门管理</h2>

    <el-tabs v-model="activeTab">
      <!-- 用户管理 Tab -->
      <el-tab-pane label="用户管理" name="users">
        <div style="margin-bottom:12px">
          <el-button type="primary" @click="openUserDialog(null)">新增用户</el-button>
        </div>
        <el-table :data="users" border stripe style="width:100%">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="realName" label="姓名" width="100" />
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
        <div style="margin-bottom:12px">
          <el-button type="primary" @click="openDeptDialog(null)">新增部门</el-button>
        </div>
        <el-table :data="depts" border stripe style="width:100%">
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
      <el-form :model="userForm" label-width="80px">
        <el-form-item label="用户名" v-if="!editingUser">
          <el-input v-model="userForm.username" placeholder="登录用户名" />
        </el-form-item>
        <el-form-item label="密码" v-if="!editingUser">
          <el-input v-model="userForm.password" type="password" placeholder="登录密码" />
        </el-form-item>
        <el-form-item label="密码" v-else>
          <el-input v-model="userForm.password" type="password" placeholder="留空则不修改" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="userForm.realName" placeholder="真实姓名" />
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
      <el-form :model="deptForm" label-width="80px">
        <el-form-item label="部门名称">
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

function roleLabel(key) {
  return dictionaryStore.labelOf('role_key', key)
}

// ---- Dept name map ----
const deptNameMap = computed(() => {
  const m = { 0: '（顶级）' }
  depts.value.forEach(d => { m[d.id] = d.deptName })
  return m
})

// ---- User Dialog ----
const userDialogVisible = ref(false)
const editingUser = ref(null)
const userDialogTitle = computed(() => editingUser.value ? '编辑用户' : '新增用户')
const userForm = ref({
  username: '',
  password: '',
  realName: '',
  deptId: null,
  selectedRoles: []
})

function openUserDialog(user) {
  editingUser.value = user
  if (user) {
    userForm.value = {
      username: user.username,
      password: '',
      realName: user.realName,
      deptId: user.deptId,
      selectedRoles: [...(user.roleKeys || [])]
    }
  } else {
    userForm.value = {
      username: '',
      password: '',
      realName: '',
      deptId: null,
      selectedRoles: []
    }
  }
  userDialogVisible.value = true
}

async function handleSaveUser() {
  const f = userForm.value
  if (!editingUser.value) {
    if (!f.username || !f.password || !f.realName) {
      ElMessage.warning('用户名、密码和姓名为必填项')
      return
    }
  }
  saving.value = true
  try {
    if (editingUser.value) {
      const payload = {
        realName: f.realName,
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
        deptId: f.deptId,
        roleKeys: f.selectedRoles.join(',')
      })
      ElMessage.success('用户创建成功')
    }
    userDialogVisible.value = false
    await loadUsers()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

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

// ---- Dept Dialog ----
const deptDialogVisible = ref(false)
const editingDept = ref(null)
const deptDialogTitle = computed(() => editingDept.value ? '编辑部门' : '新增部门')
const deptForm = ref({ deptName: '', parentId: null })

function openDeptDialog(dept) {
  editingDept.value = dept
  if (dept) {
    deptForm.value = { deptName: dept.deptName, parentId: dept.parentId || null }
  } else {
    deptForm.value = { deptName: '', parentId: null }
  }
  deptDialogVisible.value = true
}

async function handleSaveDept() {
  const f = deptForm.value
  if (!f.deptName) {
    ElMessage.warning('部门名称不能为空')
    return
  }
  saving.value = true
  try {
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
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

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

// ---- Load data ----
async function loadUsers() {
  try { users.value = await api.adminUsers() } catch (e) { /* ignore */ }
}
async function loadDepts() {
  try { depts.value = await api.adminDepts() } catch (e) { /* ignore */ }
}
async function loadRoles() {
  try { allRoles.value = await api.adminRoles() } catch (e) { /* ignore */ }
}

onMounted(() => {
  loadUsers()
  loadDepts()
  loadRoles()
})
</script>
