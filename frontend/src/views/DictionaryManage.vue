<template>
  <div class="dictionary-page">
    <div class="panel report-header">
      <h3>字典管理</h3>
    </div>
    <div class="page-actions">
      <el-button type="primary" @click="openType()">新增类型</el-button>
    </div>
    <div class="dictionary-stack">
      <section class="panel dictionary-section">
        <div class="section-title">
          <h3>字典类型</h3>
        </div>
        <el-table
          :data="paginatedTypes"
          border
          stripe
          row-key="dictType"
          class="type-table"
          :row-class-name="typeRowClassName"
          @row-click="selectTypeRow"
        >
          <el-table-column prop="dictName" label="类型名称" min-width="150" />
          <el-table-column prop="dictType" label="类型代码" min-width="150" />
          <el-table-column label="状态" width="82">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="180" />
          <el-table-column label="操作" width="106">
            <template #default="{ row }"><el-button size="small" @click.stop="openType(row)">编辑类型</el-button></template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-if="types.length > typePageSize"
          class="type-pagination"
          background
          layout="prev, pager, next"
          :page-size="typePageSize"
          :current-page="typePage"
          :total="types.length"
          @current-change="changeTypePage"
        />
      </section>

      <section class="panel dictionary-section">
        <div class="section-title">
          <h3>字典项目<span v-if="selectedTypeTitle">：{{ selectedTypeTitle }}</span></h3>
          <el-button type="primary" :disabled="!selectedType" @click="openItem()">新增项目</el-button>
        </div>
        <el-empty v-if="!selectedType" description="请选择上方字典类型" />
        <el-table v-else :data="items" border stripe>
          <el-table-column prop="dictCode" label="代码" min-width="140" />
          <el-table-column prop="dictLabel" label="显示值" min-width="120" />
          <el-table-column prop="sortOrder" label="排序" width="70" />
          <el-table-column label="状态" width="68">
            <template #default="{ row }">{{ row.enabled ? '启用' : '停用' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="82">
            <template #default="{ row }"><el-button size="small" @click="openItem(row)">编辑</el-button></template>
          </el-table-column>
        </el-table>
      </section>
    </div>

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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../api'
import { useDictionaryStore } from '../stores/dictionary'

const dictionaryStore = useDictionaryStore()
const types = ref([])
const items = ref([])
const selectedType = ref('')
const typePage = ref(1)
const typePageSize = 10
const typeDialog = ref(false)
const itemDialog = ref(false)
const editingType = ref(null)
const editingItem = ref(null)
const saving = ref(false)
const typeForm = reactive({ dictType: '', dictName: '', enabled: true, remark: '' })
const itemForm = reactive({ dictCode: '', dictLabel: '', sortOrder: 0, enabled: true, remark: '' })
const selectedDictionaryType = computed(() => types.value.find((type) => type.dictType === selectedType.value))
const selectedTypeTitle = computed(() => {
  if (!selectedDictionaryType.value) return ''
  return `${selectedDictionaryType.value.dictName}（${selectedDictionaryType.value?.dictType}）`
})
const paginatedTypes = computed(() => {
  const start = (typePage.value - 1) * typePageSize
  return types.value.slice(start, start + typePageSize)
})

async function loadTypes() {
  types.value = await api.adminDictionaryTypes()
  if (!types.value.some((type) => type.dictType === selectedType.value)) {
    selectedType.value = types.value[0]?.dictType || ''
  }
  syncTypePageWithSelection()
  if (selectedType.value) {
    await loadItems()
  } else {
    items.value = []
  }
}

function syncTypePageWithSelection() {
  const selectedIndex = types.value.findIndex((type) => type.dictType === selectedType.value)
  typePage.value = selectedIndex >= 0 ? Math.floor(selectedIndex / typePageSize) + 1 : 1
}

async function changeTypePage(page) {
  typePage.value = page
  const firstVisibleType = paginatedTypes.value[0]?.dictType
  if (firstVisibleType && !paginatedTypes.value.some((type) => type.dictType === selectedType.value)) {
    await selectType(firstVisibleType)
  }
}

async function loadItems() {
  items.value = selectedType.value ? await api.adminDictionaryItems(selectedType.value) : []
}

async function selectType(dictType) {
  if (!dictType) return
  selectedType.value = dictType
  await loadItems()
}

function selectTypeRow(row) {
  selectType(row.dictType)
}

function typeRowClassName({ row }) {
  return row.dictType === selectedType.value ? 'selected-type-row' : ''
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
.dictionary-stack {
  display: grid;
  gap: 14px;
}

.dictionary-section {
  padding: 14px;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}

.section-title h3 {
  margin: 0;
  font-size: 16px;
}

.type-table :deep(.el-table__row) {
  cursor: pointer;
}

.type-table :deep(.selected-type-row > td) {
  background: #eef7ff !important;
}

.type-pagination {
  justify-content: flex-end;
  margin-top: 12px;
}

@media (max-width: 600px) {
  .section-title {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
