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
          主页概览
        </router-link>
        <router-link to="/dashboard/students">
          学生管理
        </router-link>
        <router-link to="/dashboard/courses">
          课程管理
        </router-link>
        <router-link to="/dashboard/teachers">
          教师管理
        </router-link>
      </template>

      <template v-if="authStore.isTeacher">
        <router-link to="/teacher/dashboard">
          我的课程
        </router-link>
      </template>

      <template v-if="authStore.isStudent">
        <router-link to="/student/profile">
          我的档案
        </router-link>
        <router-link to="/student/courses">
          我的课程
        </router-link>
        <router-link to="/student/available-courses">
          选课中心
        </router-link>
        <router-link to="/student/timetable">
          我的课表
        </router-link>
      </template>
    </nav>

    <div class="sidebar-footer">
      <a @click="authStore.logout()" class="logout-button">
        登出
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
  background-color: var(--color-text-primary);
  color: var(--color-surface);
  height: 100vh;
  box-shadow: var(--box-shadow);
}

.profile-section {
  display: flex;
  align-items: center;
  padding: var(--spacing-lg);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  margin-right: var(--spacing-md);
  color: var(--color-surface);
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: 600;
  font-size: 1.1em;
  color: var(--color-surface);
}

.user-role {
  font-size: 0.8em;
  color: var(--color-border);
}

.navigation {
  flex-grow: 1;
  padding: var(--spacing-md) 0;
}

.navigation a {
  display: flex;
  align-items: center;
  padding: var(--spacing-sm) var(--spacing-lg);
  color: var(--color-surface);
  text-decoration: none;
  font-size: 1em;
  transition: background-color var(--transition-speed) ease;
}

.navigation a:hover {
  background-color: var(--color-primary-light);
  color: var(--color-primary);
}

.navigation a.router-link-exact-active {
  background-color: var(--color-primary);
  color: var(--color-surface);
  font-weight: bold;
}

.sidebar-footer {
  padding: var(--spacing-md) var(--spacing-lg);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.logout-button {
  display: flex;
  align-items: center;
  cursor: pointer;
  width: 100%;
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--border-radius);
  text-align: center;
  justify-content: center;
  transition: background-color var(--transition-speed) ease;
  color: var(--color-surface);
  background-color: rgba(255, 255, 255, 0.05);
  box-sizing: border-box;
}

.logout-button:hover {
  background-color: var(--color-danger);
  color: var(--color-surface);
}
</style>