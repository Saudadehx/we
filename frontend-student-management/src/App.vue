<template>
  <div id="app-container">
    <Notification />
    <header class="app-header" v-if="!isLoginPage">
      <h1>学生管理系统</h1>
      <nav>
        <router-link to="/students">学生列表</router-link>
        <a v-if="isLoggedIn" @click="logout" class="logout-link">登出</a>
      </nav>
    </header>

    <main class="app-main" :class="{ 'center-content': isLoginPage }">
      <router-view />
    </main>

    <footer class="app-footer" v-if="!isLoginPage">
      <p>&copy; {{ new Date().getFullYear() }} 学生管理系统</p>
    </footer>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { isLoggedIn } from '@/services/authStore';
import { authService } from '@/services/apiService';
import Notification from '@/components/Notification.vue';
const router = useRouter();
const route = useRoute();

// 计算当前是否为登录页
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
  min-height: 100vh;
  width: 100vw; /* 确保占满视口宽度 */
  background-color: #f4f7f6;
}

.app-header {
  background-color: #333;
  color: white;
  padding: 15px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0; /* 防止页眉被压缩 */
}

.app-header h1 { margin: 0; }
.app-header nav a { color: white; text-decoration: none; margin-left: 15px; }

.app-main {
  flex-grow: 1; /* 占据所有剩余空间 */
  display: flex; /* 默认为flex布局，方便其子元素（仪表盘）撑开 */
  overflow: hidden; /* 防止内容溢出时出现双滚动条 */
}

/* 当 main 元素有 center-content 这个 class 时，使其内部内容居中 */
.app-main.center-content {
  justify-content: center;
  align-items: center;
}

.app-footer {
  text-align: center;
  padding: 10px;
  border-top: 1px solid #eee;
  background-color: #fff;
  flex-shrink: 0; /* 防止页脚被压缩 */
}
.logout-link { cursor: pointer; }
</style>