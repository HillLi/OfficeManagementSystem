// 应用入口文件：初始化 Vue 实例并挂载插件
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
import './style.css'
import App from './App.vue'

// 创建 Vue 应用，注册 ElementPlus（中文语言包）、路由和 Pinia 状态管理，挂载到 #app
createApp(App).use(ElementPlus, { locale: zhCn }).use(router).use(createPinia()).mount('#app')
