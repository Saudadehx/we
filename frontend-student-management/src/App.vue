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
        <template v-if="isAdmin">
          <router-link to="/dashboard">主页概览</router-link>
          <router-link to="/students">学生列表</router-link>
        </template>
        <template v-if="!isAdmin">
          <router-link to="/my-profile">我的档案</router-link>
        </template>

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
import { useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const route = useRoute();
const authStore = useAuthStore();
const isAdmin = computed(() => authStore.isAdmin);
const isLoginPage = computed(() => route.name === 'Login');
const isLoggedIn = computed(() => authStore.isLoggedIn);
const logout = () => {
  authStore.logout();
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
  padding: 0 var(--spacing-xl); /* 使用变量 */
  height: 64px;
  background-color: var(--color-surface); /* 使用变量 */
  border-bottom: 1px solid var(--color-border); /* 使用变量 */
  flex-shrink: 0;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md); /* 使用变量，增加间距 */
}

.logo {
  width: 32px;
  height: 32px;
  color: var(--color-primary); /* 使用变量 */
}

.header-left h1 {
  margin: 0;
  font-size: 1.4em;
  font-weight: 600;
  color: var(--color-text-primary); /* 使用变量 */
}

.header-nav {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm); /* 使用变量 */
}

.header-nav a {
  text-decoration: none;
  color: var(--color-text-secondary); /* 使用变量 */
  font-weight: 500;
  padding: var(--spacing-sm) var(--spacing-md); /* 使用变量 */
  border-radius: var(--border-radius); /* 使用变量 */
  transition: all var(--transition-speed) ease; /* 使用变量 */
}

.header-nav a:hover {
  background-color: var(--color-primary-light); /* 使用变量 */
  color: var(--color-primary); /* 使用变量 */
}

/* 当前激活的路由链接样式 */
.header-nav a.router-link-exact-active {
  background-color: var(--color-primary); /* 使用变量 */
  color: var(--color-surface);
}

.logout-link {
  cursor: pointer;
}

.app-main {
  flex-grow: 1;
  display: flex;
  overflow: auto;
  background-color: var(--color-background); /* 使用变量 */
}

.app-main.center-content {
  justify-content: center;
  align-items: center;
}
</style>