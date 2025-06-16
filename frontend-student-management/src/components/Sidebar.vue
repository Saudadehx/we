<template>
  <aside class="sidebar-container">
    <div class="profile-section">
      <div class="avatar">{{ userInitial }}</div>
      <div class="user-info">
        <span class="user-name">{{ username }}</span>
        <span class="user-role">{{ userRoleText }}</span>
      </div>
    </div>

    <nav class="navigation">
      <template v-if="authStore.isAdmin">
        <router-link to="/dashboard/home">
          <span>&#128202;</span> 主页概览
        </router-link>
        <router-link to="/dashboard/students">
          <span>&#128101;</span> 学生管理
        </router-link>
        <router-link to="/dashboard/courses">
          <span>&#128218;</span> 课程管理
        </router-link>
        <router-link to="/dashboard/teachers">
          <span>&#128188;</span> 教师管理
        </router-link>
      </template>

      <template v-if="authStore.isTeacher">
        <router-link to="/teacher/dashboard">
          <span>&#127979;</span> 我的课程
        </router-link>
      </template>

      <template v-if="authStore.isStudent">
        <router-link to="/student/profile">
          <span>&#128100;</span> 我的档案
        </router-link>
        <router-link to="/student/courses">
          <span>&#128214;</span> 我的课程
        </router-link>
      </template>
    </nav>

    <div class="sidebar-footer">
      <a @click="authStore.logout()" class="logout-button">
        <span>&#128682;</span> 登出
      </a>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { jwtDecode } from 'jwt-decode';

const authStore = useAuthStore();

const username = computed(() => {
  if (!authStore.token) return '访客';
  try {
    const decoded = jwtDecode(authStore.token);
    // 'sub' 通常是JWT中的用户名（学号/工号）
    return decoded.sub || '用户';
  } catch (e) {
    return '用户';
  }
});

const userInitial = computed(() => (username.value ? username.value.charAt(0).toUpperCase() : '?'));

const userRoleText = computed(() => {
  const role = authStore.userRole;
  if (role === 'ADMIN') return '管理员';
  if (role === 'TEACHER') return '教师';
  if (role === 'STUDENT') return '学生';
  return '未知角色';
});
</script>

<style scoped>
.sidebar-container {
  display: flex;
  flex-direction: column;
  width: 240px;
  background-color: #2c3e50;
  color: #ecf0f1;
  height: 100vh;
}

.profile-section {
  display: flex;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid #34495e;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #3498db;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  margin-right: 16px;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: 600;
  font-size: 1.1em;
}

.user-role {
  font-size: 0.8em;
  color: #bdc3c7;
}

.navigation {
  flex-grow: 1;
  padding: 16px 0;
}

.navigation a {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  color: #ecf0f1;
  text-decoration: none;
  font-size: 1em;
  transition: background-color 0.2s ease;
}

.navigation a:hover {
  background-color: #34495e;
}

.navigation a.router-link-exact-active {
  background-color: #2980b9;
  font-weight: bold;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #34495e;
}

.logout-button {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  width: 100%;
  padding: 12px;
  border-radius: 4px;
  text-align: center;
  justify-content: center;
  transition: background-color 0.2s ease;
}

.logout-button:hover {
  background-color: #c0392b;
}
</style>