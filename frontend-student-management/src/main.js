// frontend-student-management/src/main.js

import { createApp } from 'vue'
import { createPinia } from 'pinia'

//只保留对主样式文件 style.css 的导入
import './style.css'

import App from './App.vue'
import router from './router'

const app = createApp(App);
const pinia = createPinia()

app.use(router);
app.use(pinia);

app.mount('#app');