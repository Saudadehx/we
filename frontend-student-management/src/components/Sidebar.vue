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
        <router-link to="/dashboard/home" class="nav-item">
          <span class="nav-text">主页概览</span>
        </router-link>
        <router-link to="/dashboard/students" class="nav-item">
          <span class="nav-text">学生管理</span>
        </router-link>
        <router-link to="/dashboard/courses" class="nav-item">
          <span class="nav-text">课程管理</span>
        </router-link>
        <router-link to="/dashboard/majors" class="nav-item">
          <span class="nav-text">专业管理</span>
        </router-link>
        <router-link to="/dashboard/teachers" class="nav-item">
          <span class="nav-text">教师管理</span>
        </router-link>
        <router-link to="/dashboard/settings" class="nav-item">
          <span class="nav-text">系统设置</span>
        </router-link>
      </template>

      <template v-if="authStore.isTeacher">
        <router-link to="/teacher/dashboard" class="nav-item">
          <span class="nav-text">我的课程</span>
        </router-link>
      </template>

      <template v-if="authStore.isStudent">
        <router-link to="/student/profile" class="nav-item">
          <span class="nav-text">我的档案</span>
        </router-link>
        <router-link to="/student/schedule" class="nav-item">
          <span class="nav-text">课表与成绩</span>
        </router-link>
        <router-link to="/student/available-courses" class="nav-item">
          <span class="nav-text">选课中心</span>
        </router-link>
      </template>
    </nav>

    <div class="sidebar-footer">
      <button @click="authStore.logout()" class="logout-button">
        <span class="logout-text">登出</span>
      </button>
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
    console.error("Token 解码失败:", e);
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
/* 样式保持不变 */
.sidebar-container {
  position: relative;
  display: flex;
  flex-direction: column;
  width: 260px;
  height: 100vh;
  background: linear-gradient(135deg,
  rgba(55, 71, 87, 0.95) 0%,
  rgba(44, 62, 80, 0.95) 50%,
  rgba(40, 55, 71, 0.95) 100%);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  color: #ffffff;
  box-shadow:
      2px 0 20px rgba(0, 0, 0, 0.3),
      inset 1px 0 0 rgba(255, 255, 255, 0.1);
  border-right: 1px solid rgba(255, 255, 255, 0.15);
  flex-shrink: 0;
  position: sticky;
  top: 0;
  left: 0;
  z-index: 100;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 玻璃反光效果层 */
.glass-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 100%;
  background: linear-gradient(
      45deg,
      transparent 0%,
      rgba(255, 255, 255, 0.1) 25%,
      rgba(255, 255, 255, 0.05) 50%,
      transparent 75%
  );
  pointer-events: none;
  z-index: 1;
}

/* 滚动条样式 */
.sidebar-container::-webkit-scrollbar {
  width: 4px;
}

.sidebar-container::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-container::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
}

.sidebar-container::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

.profile-section {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  padding: 24px 20px;
  background: linear-gradient(135deg,
  rgba(255, 255, 255, 0.05) 0%,
  rgba(255, 255, 255, 0.02) 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.avatar {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg,
  var(--color-primary, #3498db) 0%,
  rgba(52, 152, 219, 0.8) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 600;
  margin-right: 16px;
  color: #ffffff;
  flex-shrink: 0;
  border: 2px solid rgba(255, 255, 255, 0.2);
  box-shadow:
      0 4px 12px rgba(0, 0, 0, 0.2),
      inset 0 1px 2px rgba(255, 255, 255, 0.3);
}

.user-info {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex: 1;
}

.user-name {
  font-weight: 600;
  font-size: 16px;
  color: #ffffff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.user-role {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 400;
}

.navigation {
  position: relative;
  z-index: 2;
  flex-grow: 1;
  padding: 16px 0;
}

.nav-item {
  position: relative;
  display: flex;
  align-items: center;
  padding: 0;
  margin: 2px 12px;
  color: rgba(255, 255, 255, 0.85);
  text-decoration: none;
  font-size: 15px;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.nav-text {
  display: block;
  width: 100%;
  padding: 14px 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.nav-item:hover {
  color: #ffffff;
  background: linear-gradient(135deg,
  rgba(255, 255, 255, 0.1) 0%,
  rgba(255, 255, 255, 0.05) 100%);
  transform: translateX(4px);
  box-shadow:
      0 4px 12px rgba(0, 0, 0, 0.15),
      inset 0 1px 2px rgba(255, 255, 255, 0.1);
}

.nav-item.router-link-exact-active {
  color: #ffffff;
  background: linear-gradient(135deg,
  var(--color-primary, #3498db) 0%,
  rgba(52, 152, 219, 0.9) 100%);
  font-weight: 600;
  transform: translateX(8px);
  box-shadow:
      0 6px 20px rgba(52, 152, 219, 0.3),
      inset 0 1px 2px rgba(255, 255, 255, 0.2);
}

.nav-item.router-link-exact-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(to bottom,
  rgba(255, 255, 255, 0.8) 0%,
  rgba(255, 255, 255, 0.4) 100%);
  border-radius: 0 2px 2px 0;
}

.sidebar-footer {
  position: relative;
  z-index: 2;
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: linear-gradient(135deg,
  rgba(255, 255, 255, 0.05) 0%,
  rgba(255, 255, 255, 0.02) 100%);
  backdrop-filter: blur(10px);
}

.logout-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding: 0;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg,
  rgba(231, 76, 60, 0.8) 0%,
  rgba(192, 57, 43, 0.8) 100%);
  color: #ffffff;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow:
      0 4px 12px rgba(231, 76, 60, 0.3),
      inset 0 1px 2px rgba(255, 255, 255, 0.2);
  overflow: hidden;
}

.logout-text {
  display: block;
  width: 100%;
  padding: 12px 20px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.logout-button:hover {
  background: linear-gradient(135deg,
  rgba(231, 76, 60, 1) 0%,
  rgba(192, 57, 43, 1) 100%);
  transform: translateY(-2px);
  box-shadow:
      0 8px 25px rgba(231, 76, 60, 0.4),
      inset 0 1px 2px rgba(255, 255, 255, 0.3);
}

.logout-button:active {
  transform: translateY(0);
  box-shadow:
      0 4px 12px rgba(231, 76, 60, 0.3),
      inset 0 1px 2px rgba(255, 255, 255, 0.2);
}
</style>