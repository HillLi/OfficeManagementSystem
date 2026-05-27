<template>
  <div class="dictionary-page">
    <div class="panel dictionary-toolbar">
      <div>
        <h2>字典管理</h2>
        <p>维护可选业务值。停用项目不会出现在新建表单中，历史记录仍可显示。</p>
      </div>
      <el-button type="primary" @click="openType()">新增类型</el-button>
    </div>
    <div class="dictionary-grid">
      <div class="panel">
        <el-table :data="types" border highlight-current-row @current-change="selectType">
          <el-table-column prop="dictName" label="字典类型" min-width="128" />
          <el-table-column label="状态" width="74">
            <template #default="{ row }">{{ row.enabled ? '启用' : '停用' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="78">
            <template #default="{ row }"><el-button link @click.stop="openType(row)">编辑</el-button></template>
          </el-table-column>
        </el-table>
      </div>
      <div class="panel">
        <div class="items-title">
          <h3>{{ activeType?.dictName || '请选择字典类型' }}</h3>
          <el-button v-if="activeType" type="primary" @click="openItem()">新增项目</el-button>
        </div>
        <el-table v-if="activeType" :data="items" border>
          <el-table-column prop="dictCode" label="代码" min-width="140" />
          <el-table-column prop="dictLabel" label="显示值" min-width="120" />
          <el-table-column prop="sortOrder" label="排序" width="70" />
          <el-table-column label="状态" width="68">
            <template #default="{ row }">{{ row.enabled ? '启用' : '停用' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="78">
            <template #default="{ row }"><el-button link @click="openItem(row)">编辑</el-button></template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="typeDialog" :title="editingType ? '编辑字典类型' : '新增字典类型'" width="460px">
      <el-form label-position="top">
        <el-form-item label="类型代码"><el-input v-model="typeForm.dictType" :disabled="Boolean(editingType)" /></el-form-item>
        <el-form-item label="类型名称"><el-input v-model="typeForm.dictName" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="typeForm.enabled" :disabled="editingType?.systemType" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="typeForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveType">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="itemDialog" :title="editingItem ? '编辑字典项目' : '新增字典项目'" width="460px">
      <el-form label-position="top">
        <el-form-item label="项目代码"><el-input v-model="itemForm.dictCode" :disabled="Boolean(editingItem)" /></el-form-item>
        <el-form-item label="显示值"><el-input v-model="itemForm.dictLabel" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="itemForm.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="itemForm.enabled" :disabled="editingItem?.systemItem" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="itemForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveItem">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { useDictionaryStore } from '../stores/dictionary'

const dictionaryStore = useDictionaryStore()
const types = ref([])
const items = ref([])
const selectedType = ref('')
const activeType = computed(() => types.value.find((type) => type.dictType === selectedType.value))
const typeDialog = ref(false)
const itemDialog = ref(false)
const editingType = ref(null)
const editingItem = ref(null)
const saving = ref(false)
const typeForm = reactive({ dictType: '', dictName: '', enabled: true, remark: '' })
const itemForm = reactive({ dictCode: '', dictLabel: '', sortOrder: 0, enabled: true, remark: '' })

async function loadTypes() {
  types.value = await api.adminDictionaryTypes()
  if (!selectedType.value && types.value.length) selectedType.value = types.value[0].dictType
  if (selectedType.value) await loadItems()
}

async function loadItems() {
  items.value = selectedType.value ? await api.adminDictionaryItems(selectedType.value) : []
}

function selectType(type) {
  if (!type) return
  selectedType.value = type.dictType
  loadItems()
}

function openType(type = null) {
  editingType.value = type
  Object.assign(typeForm, type || { dictType: '', dictName: '', enabled: true, remark: '' })
  typeDialog.value = true
}

async function saveType() {
  saving.value = true
  try {
    if (editingType.value) {
      await api.adminUpdateDictionaryType(editingType.value.dictType, typeForm)
    } else {
      await api.adminCreateDictionaryType(typeForm)
      selectedType.value = typeForm.dictType
    }
    typeDialog.value = false
    await loadTypes()
    await dictionaryStore.refresh(true)
    ElMessage.success('字典类型已保存')
  } finally {
    saving.value = false
  }
}

function openItem(item = null) {
  editingItem.value = item
  Object.assign(itemForm, item || { dictCode: '', dictLabel: '', sortOrder: 0, enabled: true, remark: '' })
  itemDialog.value = true
}

async function saveItem() {
  saving.value = true
  try {
    if (editingItem.value) {
      await api.adminUpdateDictionaryItem(selectedType.value, editingItem.value.dictCode, itemForm)
    } else {
      await api.adminCreateDictionaryItem(selectedType.value, itemForm)
    }
    itemDialog.value = false
    await loadItems()
    await dictionaryStore.refresh(true)
    ElMessage.success('字典项目已保存')
  } finally {
    saving.value = false
  }
}

onMounted(loadTypes)
</script>

<style scoped>
.dictionary-toolbar,
.items-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.dictionary-toolbar {
  margin-bottom: 14px;
}
.dictionary-toolbar h2,
.items-title h3 {
  margin: 0;
}
.dictionary-toolbar p {
  margin: 8px 0 0;
  color: #657487;
}
.dictionary-grid {
  display: grid;
  grid-template-columns: minmax(260px, 340px) minmax(480px, 1fr);
  gap: 14px;
}
.items-title {
  margin-bottom: 12px;
}
@media (max-width: 900px) {
  .dictionary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
