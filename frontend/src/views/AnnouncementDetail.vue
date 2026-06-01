<template>
  <div class="announcement-detail-page">
    <el-skeleton v-if="loading" :rows="7" animated />
    <el-empty v-else-if="!announcement" description="公告不存在或无权查看" />
    <article v-else class="announcement-detail">
      <div class="detail-header">
        <div>
          <div class="tag-row">
            <el-tag v-if="announcement.pinned" type="danger" size="small">置顶</el-tag>
            <el-tag size="small">{{ categoryText(announcement.category) }}</el-tag>
            <span class="scope-text">发布范围：{{ scopeText(announcement) }}</span>
          </div>
          <h1>{{ announcement.title }}</h1>
        </div>
        <span class="time">{{ formatDate(announcement.publishedAt || announcement.updatedAt || announcement.createdAt) }}</span>
      </div>
      <div class="content-text">{{ announcement.content }}</div>
    </article>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '../api'

const route = useRoute()
const loading = ref(false)
const announcement = ref(null)

async function load() {
  loading.value = true
  try {
    announcement.value = await api.announcement(route.params.id)
  } catch (error) {
    announcement.value = null
  } finally {
    loading.value = false
  }
}

function categoryText(category) {
  return { notice: '通知', announcement: '公告', policy: '制度' }[category] || '通知'
}

function scopeText(row) {
  return row.targetType === 'dept' ? (row.targetDeptName || '指定部门') : '全校'
}

function formatDate(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '-'
}

onMounted(load)
watch(() => route.params.id, load)
</script>

<style scoped>
.announcement-detail-page {
  max-width: 920px;
  margin: 0 auto;
}

.announcement-detail {
  background: #fff;
  border: 1px solid #e3e8ef;
  border-radius: 8px;
  padding: 28px 32px;
}

.detail-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  border-bottom: 1px solid #eef2f6;
  padding-bottom: 18px;
  margin-bottom: 22px;
}

.tag-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.detail-header h1 {
  margin: 14px 0 0;
  font-size: 26px;
  line-height: 1.35;
  color: #1f2d3d;
  letter-spacing: 0;
}

.scope-text,
.time {
  color: #657487;
}

.time {
  white-space: nowrap;
}

.content-text {
  color: #303946;
  font-size: 16px;
  line-height: 1.9;
  white-space: pre-wrap;
}

@media (max-width: 700px) {
  .announcement-detail {
    padding: 20px;
  }

  .detail-header {
    flex-direction: column;
  }
}
</style>
