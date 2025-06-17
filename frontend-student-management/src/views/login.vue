<template>
  <div class="login-page-wrapper">
    <!-- 背景装饰元素 -->
    <div class="bg-decoration">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
      <div class="shape shape-4"></div>
      <div class="shape shape-5"></div>
    </div>

    <!-- 主登录卡片 -->
    <div class="login-card">
      <!-- 头部区域 -->
      <div class="card-header">
        <h1 class="system-title">学生管理系统</h1>
        <h2 class="welcome-title">欢迎回来</h2>
        <p class="welcome-subtitle">请使用您的账户信息登录系统</p>
      </div>

      <!-- 登录表单 -->
      <form @submit.prevent="handleLogin" class="login-form">
        <!-- 用户名输入组 -->
        <div class="form-group">
          <label for="username" class="form-label">用户名</label>
          <input
              type="text"
              id="username"
              v-model="username"
              required
              placeholder="请输入管理员或学生用户名"
              class="form-input"
              :class="{ 'input-error': errorMessage }"
          >
        </div>

        <!-- 密码输入组 -->
        <div class="form-group">
          <label for="password" class="form-label">密码</label>
          <input
              type="password"
              id="password"
              v-model="password"
              required
              placeholder="请输入密码"
              class="form-input"
              :class="{ 'input-error': errorMessage }"
          >
        </div>

        <!-- 记住登录选项 -->
        <div class="form-options">
          <label class="checkbox-wrapper">
            <input type="checkbox" v-model="rememberMe">
            <span class="checkmark"></span>
            <span class="checkbox-label">记住登录状态</span>
          </label>
          <a href="#" class="forgot-password">忘记密码？</a>
        </div>

        <!-- 错误信息 -->
        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <!-- 登录按钮 -->
        <button type="submit" class="submit-btn" :disabled="isLoading">
          <span v-if="!isLoading">登录系统</span>
          <div v-else class="loading-spinner"></div>
        </button>
      </form>
    </div>

    <!-- 底部装饰 -->
    <div class="page-footer">
      <p>&copy; 2025 学生管理系统 - 专业 | 高效 | 安全</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';

const username = ref('');
const password = ref('');
const errorMessage = ref('');
const rememberMe = ref(false);
const isLoading = ref(false);
const authStore = useAuthStore();

const handleLogin = async () => {
  errorMessage.value = '';
  isLoading.value = true;

  try {
    const success = await authStore.login(username.value, password.value);
    if (!success) {
      errorMessage.value = '登录失败，请检查用户名或密码是否正确';
    }
  } catch (error) {
    errorMessage.value = '网络连接异常，请稍后重试';
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
/* 页面容器 */
.login-page-wrapper {
  position: relative;
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  box-sizing: border-box;
  overflow: hidden;
}

/* 背景装饰元素 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.shape-1 {
  width: 120px;
  height: 120px;
  top: 10%;
  right: 15%;
  background: rgba(255, 255, 255, 0.08);
}

.shape-2 {
  width: 80px;
  height: 80px;
  bottom: 25%;
  left: 10%;
  background: rgba(255, 255, 255, 0.06);
}

.shape-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  right: -50px;
  background: rgba(255, 255, 255, 0.04);
}

.shape-4 {
  width: 60px;
  height: 60px;
  top: 20%;
  left: 20%;
  background: rgba(255, 255, 255, 0.1);
}

.shape-5 {
  width: 150px;
  height: 150px;
  bottom: 10%;
  right: 30%;
  background: rgba(255, 255, 255, 0.05);
}

/* 添加一些几何图形装饰 */
.bg-decoration::before {
  content: '';
  position: absolute;
  top: 30%;
  left: 5%;
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  transform: rotate(45deg);
}

.bg-decoration::after {
  content: '';
  position: absolute;
  bottom: 40%;
  right: 8%;
  width: 30px;
  height: 30px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 6px;
  transform: rotate(30deg);
}

/* 登录卡片 */
.login-card {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

/* 头部区域 - 统一背景色 */
.card-header {
  padding: 40px 40px 30px;
  text-align: center;
  background: white;
}

.system-title {
  font-size: 1.2em;
  font-weight: 600;
  color: #667eea;
  margin: 0 0 20px 0;
  letter-spacing: 1px;
}

.welcome-title {
  font-size: 2em;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
}

.welcome-subtitle {
  color: #666;
  margin: 0;
  font-size: 0.9em;
}

/* 表单样式 - 统一背景色 */
.login-form {
  padding: 0 40px 40px;
  background: white;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #333;
  font-size: 0.9em;
}

.form-input {
  width: 100%;
  padding: 16px 20px;
  box-sizing: border-box;
  border: 2px solid #e8ecf3;
  border-radius: 12px;
  font-size: 1em;
  background: #f8fafc;
  font-family: inherit;
  transition: all 0.2s ease;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-input.input-error {
  border-color: #e74c3c;
  background: #fdf2f2;
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  font-size: 0.9em;
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.checkbox-wrapper input[type="checkbox"] {
  display: none;
}

.checkmark {
  width: 18px;
  height: 18px;
  border: 2px solid #e8ecf3;
  border-radius: 4px;
  margin-right: 8px;
  position: relative;
  transition: all 0.2s ease;
}

.checkbox-wrapper input[type="checkbox"]:checked + .checkmark {
  background: #667eea;
  border-color: #667eea;
}

.checkbox-wrapper input[type="checkbox"]:checked + .checkmark::after {
  content: '';
  position: absolute;
  left: 5px;
  top: 2px;
  width: 4px;
  height: 8px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.checkbox-label {
  color: #666;
  user-select: none;
}

.forgot-password {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s ease;
}

.forgot-password:hover {
  color: #5a67d8;
  text-decoration: underline;
}

/* 错误信息 */
.error-message {
  color: #e74c3c;
  background: #fdf2f2;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #e74c3c;
  font-size: 0.9em;
}

/* 登录按钮 */
.submit-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 1.1em;
  font-weight: 600;
  letter-spacing: 0.5px;
  transition: all 0.2s ease;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

/* 加载动画 - 保留 */
.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 页面底部 */
.page-footer {
  margin-top: 30px;
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.85em;
  position: relative;
  z-index: 1;
}

.page-footer p {
  margin: 0;
}
</style>