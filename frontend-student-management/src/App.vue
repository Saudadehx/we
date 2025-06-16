<template>
  <div id="app-container">
    <Notification />
    <header class="app-header" v-if="!isLoginPage">
      <div class="header-left">
        <div class="logo">
          <svg viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg"><path d="M729.6 620.8c-83.2 0-153.6 67.2-153.6 153.6s67.2 153.6 153.6 153.6c83.2 0 153.6-67.2 153.6-153.6S812.8 620.8 729.6 620.8zM294.4 620.8c-83.2 0-153.6 67.2-153.6 153.6s67.2 153.6 153.6 153.6c83.2 0 153.6-67.2 153.6-153.6S377.6 620.8 294.4 620.8zM729.6 857.6c-44.8 0-76.8-35.2-76.8-76.8s32-76.8 76.8-76.8c44.8 0 76.8 35.2 76.8 76.8S774.4 857.6 729.6 857.6zM294.4 857.6c-44.8 0-76.8-35.2-76.8-76.8s32-76.8 76.8-76.8c44.8 0 76.8 35.2 76.8 76.8S339.2 857.6 294.4 857.6zM883.2 102.4H140.8C64 102.4 0 166.4 0 243.2v230.4c0 35.2 28.8 64 64 64h896c35.2 0 64-28.8 64-64V243.2c0-76.8-64-140.8-140.8-140.8zM960 473.6c0 12.8-9.6 25.6-25.6 25.6H89.6c-12.8 0-25.6-9.6-25.6-25.6V243.2c0-44.8 32-76.8 76.8-76.8h742.4c44.8 0 76.8 32 76.8 76.8v230.4z"></path></svg>
        </div>
        <h1>学生管理系统</h1>
      </div>
      <nav class="header-nav">
        <router-link to="/dashboard">主页概览</router-link>
        <router-link to="/students">学生列表</router-link>
        <a v-if="isLoggedIn" @click="logout" class="logout-link">登出</a>
      </nav>
    </header>

    <main class="app-main" :class="{ 'center-content': isLoginPage }">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import Notification from '@/components/Notification.vue';
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { isLoggedIn } from '@/services/authStore';
import { authService } from '@/services/apiService';

const router = useRouter();
const route = useRoute();

const isLoginPage = computed(() => route.name === 'Login');

const logout = () => {
  authService.logout();
  router.push('/login');
};
</script>

<style scoped>
#app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 2rem;
  height: 64px; /* 固定高度 */
  background-color: #ffffff;
  border-bottom: 1px solid #e0e0e0;
  flex-shrink: 0;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
}
.logo {
  width: 32px;
  height: 32px;
  margin-right: 12px;
  color: #337ab7;
}
.logo svg {
  width: 100%;
  height: 100%;
}

.header-left h1 {
  margin: 0;
  font-size: 1.5em;
  font-weight: 600;
  color: #2c3e50;
}

.header-nav a {
  text-decoration: none;
  color: #555;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: 6px;
  margin-left: 8px;
  transition: background-color 0.2s ease, color 0.2s ease;
}
.header-nav a:hover {
  background-color: #f5f5f5;
  color: #337ab7;
}
.header-nav a.router-link-exact-active {
  background-color: #337ab7;
  color: white;
}
.logout-link {
  cursor: pointer;
}

.app-main {
  flex-grow: 1;
  display: flex;
  overflow: hidden;
  background-color: #f4f7f6;
}
.app-main.center-content {
  justify-content: center;
  align-items: center;
}
</style>