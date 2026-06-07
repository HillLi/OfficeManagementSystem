import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'
import './style.css'
import App from './App.vue'

createApp(App).use(ElementPlus, { locale: zhCn }).use(router).use(createPinia()).mount('#app')
