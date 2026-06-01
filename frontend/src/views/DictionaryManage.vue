<template>
  <div class="dictionary-page">
    <div class="dictionary-header">
      <div>
        <h2>字典管理</h2>
        <p>维护可选业务值。停用项目不会出现在新建表单中，历史记录仍可显示。</p>
      </div>
      <el-button type="primary" @click="openType()">新增类型</el-button>
    </div>
    <el-tabs v-model="selectedType" @tab-change="selectType">
      <el-tab-pane v-for="type in types" :key="type.dictType" :label="type.dictName" :name="type.dictType" lazy>
        <div class="items-title">
          <div class="type-summary">
            <el-tag :type="type.enabled ? 'success' : 'info'">{{ type.enabled ? '启用' : '停用' }}</el-tag>
            <span>{{ type.remark || '维护该类型下的字典项目' }}</span>
          </div>
          <div class="item-actions">
            <el-button @click="openType(type)">编辑类型</el-button>
            <el-button type="primary" @click="openItem()">新增项目</el-button>
          </div>
        </div>
        <el-table :data="items" border stripe>
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
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="typeDialog" :title="editingType ? '编辑字典类型' : '新增字典类型'" width="460px" :close-on-click-modal="false">
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

    <el-dialog v-model="itemDialog" :title="editingItem ? '编辑字典项目' : '新增字典项目'" width="460px" :close-on-click-modal="false">
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { useDictionaryStore } from '../stores/dictionary'

const dictionaryStore = useDictionaryStore()
const types = ref([])
const items = ref([])
const selectedType = ref('')
const typeDialog = ref(false)
const itemDialog = ref(false)
const editingType = ref(null)
const editingItem = ref(null)
const saving = ref(false)
const typeForm = reactive({ dictType: '', dictName: '', enabled: true, remark: '' })
const itemForm = reactive({ dictCode: '', dictLabel: '', sortOrder: 0, enabled: true, remark: '' })

async function loadTypes() {
  types.value = await api.adminDictionaryTypes()
  if (!types.value.some((type) => type.dictType === selectedType.value)) {
    selectedType.value = types.value[0]?.dictType || ''
  }
  if (selectedType.value) await loadItems()
}

async function loadItems() {
  items.value = selectedType.value ? await api.adminDictionaryItems(selectedType.value) : []
}

function selectType(dictType) {
  if (!dictType) return
  selectedType.value = dictType
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
.dictionary-header,
.items-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.dictionary-header {
  margin-bottom: 14px;
}
.dictionary-header h2 {
  margin: 0;
}
.dictionary-header p {
  margin: 8px 0 0;
  color: #657487;
}
.items-title {
  margin-bottom: 12px;
}
.type-summary,
.item-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.type-summary {
  color: #657487;
}
@media (max-width: 600px) {
  .dictionary-header,
  .items-title {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
