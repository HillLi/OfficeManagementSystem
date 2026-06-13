<template>
  <div class="not-found-page">
    <section class="panel not-found-panel">
      <h3>页面不存在</h3>
      <p>请从左侧菜单进入可访问的功能页面。</p>
      <el-button type="primary" @click="$router.push('/dashboard')">返回首页</el-button>
    </section>
  </div>
</template>

// 404页面：当访问不存在的路由时显示的提示页面
<script setup>
</script>

<style scoped>
.not-found-page {
  min-height: 360px;
  display: grid;
  place-items: center;
}

.not-found-panel {
  width: min(420px, 100%);
  text-align: center;
}

.not-found-panel h3 {
  margin: 0 0 10px;
  font-size: 20px;
}

.not-found-panel p {
  margin: 0 0 16px;
  color: #667085;
}
</style>
