import { createApp } from 'vue'
import { createPinia } from 'pinia'

//现在只导入主样式文件 style.css
import './style.css'

import App from './App.vue'
import router from './router'

const app = createApp(App);
const pinia = createPinia()

app.use(router);
app.use(pinia);

app.mount('#app');