import { createApp } from 'vue'
import './style.css' // Vite's default global styles
import App from './App.vue'
import router from './router' // Import the router configuration

const app = createApp(App);

app.use(router); // Integrate the router with the Vue application

app.mount('#app');
