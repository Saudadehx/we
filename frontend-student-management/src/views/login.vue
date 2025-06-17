<template>
  <div class="login-page-wrapper">
    <!-- 背景装饰元素 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <!-- 主登录卡片 -->
    <div class="login-card">
      <!-- 顶部装饰条 -->
      <div class="card-accent"></div>

      <!-- 头部区域 -->
      <div class="card-header">
        <div class="logo-section">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L13.09 8.26L20 9L13.09 9.74L12 16L10.91 9.74L4 9L10.91 8.26L12 2Z"
                    fill="currentColor"/>
            </svg>
          </div>
          <h1 class="system-title">学生管理系统</h1>
        </div>
        <h2 class="welcome-title">欢迎回来</h2>
        <p class="welcome-subtitle">请使用您的账户信息登录系统</p>
      </div>

      <!-- 登录表单 -->
      <form @submit.prevent="handleLogin" class="login-form">
        <!-- 用户名输入组 -->
        <div class="form-group">
          <label for="username" class="form-label">
            <span class="label-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 12C14.21 12 16 10.21 16 8C16 5.79 14.21 4 12 4C9.79 4 8 5.79 8 8C8 10.21 9.79 12 12 12ZM12 14C9.33 14 4 15.34 4 18V20H20V18C20 15.34 14.67 14 12 14Z"
                      fill="currentColor"/>
              </svg>
            </span>
            用户名
          </label>
          <div class="input-wrapper">
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
        </div>

        <!-- 密码输入组 -->
        <div class="form-group">
          <label for="password" class="form-label">
            <span class="label-icon">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M6 10V8C6 5.79086 7.79086 4 10 4H14C16.2091 4 18 5.79086 18 8V10H19C20.1046 10 21 10.8954 21 12V20C21 21.1046 20.1046 22 19 22H5C3.89543 22 3 21.1046 3 20V12C3 10.8954 3.89543 10 5 10H6ZM8 8V10H16V8C16 6.89543 15.1046 6 14 6H10C8.89543 6 8 6.89543 8 8Z"
                      fill="currentColor"/>
              </svg>
            </span>
            密码
          </label>
          <div class="input-wrapper">
            <input
                :type="showPassword ? 'text' : 'password'"
                id="password"
                v-model="password"
                required
                placeholder="请输入密码"
                class="form-input"
                :class="{ 'input-error': errorMessage }"
            >
            <button
                type="button"
                class="password-toggle"
                @click="showPassword = !showPassword"
            >
              <svg v-if="showPassword" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 4.5C7 4.5 2.73 7.61 1 12C2.73 16.39 7 19.5 12 19.5S21.27 16.39 23 12C21.27 7.61 17 4.5 12 4.5ZM12 17C9.24 17 7 14.76 7 12S9.24 7 12 7S17 9.24 17 12S14.76 17 12 17ZM12 9C10.34 9 9 10.34 9 12S10.34 15 12 15S15 13.66 15 12S13.66 9 12 9Z"
                      fill="currentColor"/>
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 7C9.24 7 7 9.24 7 12C7 12.65 7.13 13.26 7.36 13.82L9.8 11.38C9.93 10.59 10.59 9.93 11.38 9.8L13.82 7.36C13.26 7.13 12.65 7 12 7ZM2 4.27L4.28 6.55L4.73 7C3.08 8.3 1.78 10 1 12C2.73 16.39 7 19.5 12 19.5C13.55 19.5 15.03 19.2 16.38 18.66L16.81 19.09L19.73 22L21 20.73L3.27 3L2 4.27ZM7.53 9.8L9.08 11.35C9.03 11.56 9 11.78 9 12C9 13.66 10.34 15 12 15C12.22 15 12.44 14.97 12.65 14.92L14.2 16.47C13.53 16.8 12.79 17 12 17C9.24 17 7 14.76 7 12C7 11.21 7.2 10.47 7.53 9.8ZM15 12.16L11.84 9C11.89 9 11.94 9 12 9C13.66 9 15 10.34 15 12C15 12.06 15 12.11 15 12.16Z"
                      fill="currentColor"/>
              </svg>
            </button>
          </div>
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
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2C6.48 2 2 6.48 2 12S6.48 22 12 22 22 17.52 22 12 17.52 2 12 2ZM13 17H11V15H13V17ZM13 13H11V7H13V13Z"
                  fill="currentColor"/>
          </svg>
          {{ errorMessage }}
        </div>

        <!-- 登录按钮 -->
        <button type="submit" class="submit-btn" :disabled="isLoading">
          <span v-if="!isLoading" class="btn-content">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M10.09 15.59L11.5 17L16.5 12L11.5 7L10.09 8.41L12.67 11H3V13H12.67L10.09 15.59ZM19 3H5C3.89 3 3 3.9 3 5V9H5V5H19V19H5V15H3V19C3 20.1 3.89 21 5 21H19C20.1 21 21 20.1 21 19V5C21 3.9 20.1 3 19 3Z"
                    fill="currentColor"/>
            </svg>
            登录系统
          </span>
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
const showPassword = ref(false);
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
  background: linear-gradient(135deg,
  var(--color-background) 0%,
  #e8f2f7 50%,
  var(--color-primary-light) 100%);
  padding: var(--spacing-lg);
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

.circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(45deg,
  rgba(var(--color-primary-rgb), 0.1),
  rgba(var(--color-primary-rgb), 0.05));
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  right: 15%;
  animation-delay: 0s;
}

.circle-2 {
  width: 150px;
  height: 150px;
  bottom: 20%;
  left: 10%;
  animation-delay: 2s;
}

.circle-3 {
  width: 100px;
  height: 100px;
  top: 60%;
  right: 70%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

/* 登录卡片 */
.login-card {
  position: relative;
  width: 100%;
  max-width: 450px;
  background: var(--color-surface);
  border-radius: calc(var(--border-radius) * 2);
  box-shadow:
      0 20px 40px rgba(0, 0, 0, 0.1),
      0 8px 16px rgba(0, 0, 0, 0.06),
      0 0 0 1px rgba(var(--color-primary-rgb), 0.05);
  overflow: hidden;
  animation: slideUp 0.8s cubic-bezier(0.22, 1, 0.36, 1);
  z-index: 1;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 顶部装饰条 */
.card-accent {
  height: 4px;
  background: linear-gradient(90deg,
  var(--color-primary),
  rgba(var(--color-primary-rgb), 0.8),
  var(--color-primary));
  position: relative;
}

.card-accent::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg,
  transparent,
  rgba(255, 255, 255, 0.3),
  transparent);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

/* 头部区域 */
.card-header {
  padding: var(--spacing-xl) var(--spacing-xl) var(--spacing-lg);
  text-align: center;
  background: linear-gradient(180deg,
  rgba(var(--color-primary-rgb), 0.02) 0%,
  transparent 100%);
}

.logo-section {
  margin-bottom: var(--spacing-lg);
}

.logo-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto var(--spacing-md);
  background: linear-gradient(135deg, var(--color-primary), rgba(var(--color-primary-rgb), 0.8));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 24px rgba(var(--color-primary-rgb), 0.3);
}

.logo-icon svg {
  width: 24px;
  height: 24px;
}

.system-title {
  font-size: 1.1em;
  font-weight: 600;
  color: var(--color-text-secondary);
  margin: 0;
  letter-spacing: 2px;
}

.welcome-title {
  font-size: 2.2em;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: var(--spacing-md) 0 var(--spacing-sm);
  letter-spacing: -0.5px;
}

.welcome-subtitle {
  color: var(--color-text-secondary);
  margin: 0;
  font-size: 0.95em;
  line-height: 1.5;
}

/* 表单样式 */
.login-form {
  padding: 0 var(--spacing-xl) var(--spacing-lg);
}

.form-group {
  margin-bottom: var(--spacing-lg);
}

.form-label {
  display: flex;
  align-items: center;
  margin-bottom: var(--spacing-sm);
  font-weight: 600;
  color: var(--color-text-primary);
  font-size: 0.9em;
  letter-spacing: 0.5px;
}

.label-icon {
  width: 18px;
  height: 18px;
  margin-right: var(--spacing-sm);
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.label-icon svg {
  width: 16px;
  height: 16px;
}

.input-wrapper {
  position: relative;
}

.form-input {
  width: 100%;
  padding: 16px 20px;
  box-sizing: border-box;
  border: 2px solid var(--color-border);
  border-radius: var(--border-radius);
  font-size: 1em;
  background: #fcfdff;
  transition: all var(--transition-speed) cubic-bezier(0.4, 0, 0.2, 1);
  font-family: inherit;
}

.form-input:focus {
  outline: none;
  border-color: var(--color-primary);
  background: white;
  box-shadow:
      0 0 0 3px rgba(var(--color-primary-rgb), 0.1),
      0 4px 12px rgba(var(--color-primary-rgb), 0.15);
  transform: translateY(-1px);
}

.form-input.input-error {
  border-color: var(--color-danger);
  background: rgba(220, 53, 69, 0.02);
}

.form-input.input-error:focus {
  border-color: var(--color-danger);
  box-shadow:
      0 0 0 3px rgba(220, 53, 69, 0.1),
      0 4px 12px rgba(220, 53, 69, 0.15);
}

/* 密码显示切换按钮 */
.password-toggle {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--color-text-secondary);
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all var(--transition-speed) ease;
}

.password-toggle:hover {
  color: var(--color-primary);
  background: rgba(var(--color-primary-rgb), 0.1);
}

.password-toggle svg {
  width: 20px;
  height: 20px;
}

/* 表单选项 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
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
  border: 2px solid var(--color-border);
  border-radius: 4px;
  margin-right: var(--spacing-sm);
  position: relative;
  transition: all var(--transition-speed) ease;
}

.checkbox-wrapper input[type="checkbox"]:checked + .checkmark {
  background: var(--color-primary);
  border-color: var(--color-primary);
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
  color: var(--color-text-secondary);
  user-select: none;
}

.forgot-password {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
  transition: color var(--transition-speed) ease;
}

.forgot-password:hover {
  color: rgba(var(--color-primary-rgb), 0.8);
  text-decoration: underline;
}

/* 错误信息 */
.error-message {
  display: flex;
  align-items: center;
  color: var(--color-danger);
  background: linear-gradient(135deg,
  rgba(220, 53, 69, 0.08),
  rgba(220, 53, 69, 0.05));
  padding: var(--spacing-md);
  border-radius: var(--border-radius);
  margin-bottom: var(--spacing-lg);
  border-left: 4px solid var(--color-danger);
  font-size: 0.9em;
  animation: shake 0.5s ease-in-out;
}

.error-message svg {
  width: 18px;
  height: 18px;
  margin-right: var(--spacing-sm);
  flex-shrink: 0;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

/* 登录按钮 */
.submit-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, var(--color-primary), rgba(var(--color-primary-rgb), 0.9));
  color: white;
  border: none;
  border-radius: var(--border-radius);
  cursor: pointer;
  font-size: 1.1em;
  font-weight: 600;
  transition: all var(--transition-speed) cubic-bezier(0.4, 0, 0.2, 1);
  letter-spacing: 1px;
  position: relative;
  overflow: hidden;
  box-shadow:
      0 4px 16px rgba(var(--color-primary-rgb), 0.3),
      0 2px 4px rgba(var(--color-primary-rgb), 0.2);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow:
      0 8px 24px rgba(var(--color-primary-rgb), 0.4),
      0 4px 8px rgba(var(--color-primary-rgb), 0.3);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.btn-content {
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-content svg {
  width: 20px;
  height: 20px;
  margin-right: var(--spacing-sm);
}

/* 加载动画 */
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

/* 卡片底部 */
.card-footer {
  padding: var(--spacing-lg) var(--spacing-xl) var(--spacing-xl);
  text-align: center;
  border-top: 1px solid rgba(var(--color-primary-rgb), 0.1);
  background: rgba(var(--color-primary-rgb), 0.02);
}

.footer-text {
  color: var(--color-text-secondary);
  font-size: 0.9em;
  margin: 0 0 var(--spacing-sm);
}

.version-info {
  color: var(--color-text-secondary);
  font-size: 0.8em;
  opacity: 0.7;
}

/* 页面底部 */
.page-footer {
  margin-top: var(--spacing-xl);
  text-align: center;
  color: var(--color-text-secondary);
  font-size: 0.85em;
  z-index: 1;
}

.page-footer p {
  margin: 0;
  opacity: 0.8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-page-wrapper {
    padding: var(--spacing-md);
  }

  .login-card {
    max-width: 100%;
  }

  .card-header {
    padding: var(--spacing-lg) var(--spacing-lg) var(--spacing-md);
  }

  .login-form {
    padding: 0 var(--spacing-lg) var(--spacing-md);
  }

  .card-footer {
    padding: var(--spacing-md) var(--spacing-lg);
  }

  .welcome-title {
    font-size: 1.8em;
  }

  .form-options {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--spacing-sm);
  }

  .circle {
    display: none;
  }
}

@media (max-width: 480px) {
  .welcome-title {
    font-size: 1.6em;
  }

  .form-input {
    padding: 14px 16px;
  }

  .submit-btn {
    padding: 14px;
    font-size: 1em;
  }
}

/* 高对比度和可访问性 */
@media (prefers-reduced-motion: reduce) {
  * {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}

/* 深色模式支持（可选） */
@media (prefers-color-scheme: dark) {
  .login-page-wrapper {
    background: linear-gradient(135deg, #1a1a1a 0%, #2c2c2c 50%, #1e1e1e 100%);
  }

  .login-card {
    background: #2d2d2d;
    border: 1px solid #404040;
  }

  .form-input {
    background: #3a3a3a;
    border-color: #505050;
    color: #ffffff;
  }

  .form-input:focus {
    background: #404040;
  }
}
</style>