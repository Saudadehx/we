<template>
  <div id="app-container">
    <header class="app-header">
      <h1>学生管理系统</h1>
      <nav>
        <router-link to="/">学生列表</router-link>
        <a v-if="isLoggedIn" @click="logout" class="logout-link">登出</a>
      </nav>
    </header>
    <main class="app-main">
      <router-view></router-view>
    </main>
    <footer class="app-footer">
      <p>&copy; {{ new Date().getFullYear() }} 学生管理系统</p>
    </footer>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { authService } from '@/services/apiService';

const router = useRouter();

// 计算属性，判断用户是否登录
const isLoggedIn = computed(() => !!localStorage.getItem('token'));

const logout = () => {
  authService.logout();
  // 登出后跳转到登录页
  router.push('/login');
};
</script>

<style scoped>
#app-container {
  font-family: 'Arial', sans-serif;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.app-header {
  background-color: #333;
  color: white;
  padding: 15px 30px;
  text-align: center;
}

.app-header h1 {
  margin: 0;
  font-size: 1.8em;
}

.app-header nav {
  margin-top: 10px;
}

.app-header nav a {
  color: white;
  text-decoration: none;
  padding: 5px 10px;
  border-radius: 4px;
}

.app-header nav a:hover,
.app-header nav a.router-link-exact-active {
  background-color: #555;
}

.app-main {
  flex-grow: 1;
  padding: 20px;
}

.app-footer {
  background-color: #f8f8f8;
  color: #333;
  text-align: center;
  padding: 10px;
  border-top: 1px solid #eee;
  font-size: 0.9em;
}
.logout-link {
  color: white;
  text-decoration: none;
  padding: 5px 10px;
  border-radius: 4px;
  margin-left: 15px;
  cursor: pointer;
}

.logout-link:hover {
  background-color: #f44336; /* 红色背景高亮 */
}
</style>
