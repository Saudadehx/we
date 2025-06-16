<template>
  <div class="login-page-wrapper">
    <div class="login-card">
      <div class="card-header">
        <h2>欢迎回来</h2>
        <p>登录您的学生管理系统账户</p>
      </div>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">用户名</label>
          <input type="text" id="username" v-model="username" required placeholder="请输入管理员或学生用户名">
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input type="password" id="password" v-model="password" required placeholder="请输入密码">
        </div>
        <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
        <button type="submit" class="submit-btn">登 录</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';

const username = ref('');
const password = ref('');
const errorMessage = ref('');
const authStore = useAuthStore();

const handleLogin = async () => {
  errorMessage.value = '';
  const success = await authStore.login(username.value, password.value);
  if (!success) {
    errorMessage.value = '登录失败，请检查用户名或密码。';
  }
};
</script>

<style scoped>
.login-page-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--color-background); /* 使用变量 */
}

.login-card {
  width: 400px;
  padding: var(--spacing-xl); /* 使用变量 */
  background-color: var(--color-surface); /* 使用变量 */
  border-radius: var(--border-radius); /* 使用变量 */
  box-shadow: var(--box-shadow); /* 使用变量 */
  animation: fadeIn 0.5s ease-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.card-header {
  text-align: center;
  margin-bottom: var(--spacing-lg); /* 使用变量 */
}
.card-header h2 {
  margin: 0;
  font-size: 2em;
  font-weight: 700;
  color: var(--color-text-primary); /* 使用变量 */
}
.card-header p {
  margin-top: var(--spacing-sm); /* 使用变量 */
  color: var(--color-text-secondary); /* 使用变量 */
}

.form-group {
  margin-bottom: var(--spacing-lg); /* 使用变量 */
}
.form-group label {
  display: block;
  margin-bottom: var(--spacing-sm); /* 使用变量 */
  font-weight: 600;
  color: var(--color-text-primary);
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  box-sizing: border-box;
  border: 1px solid var(--color-border); /* 使用变量 */
  border-radius: var(--border-radius); /* 使用变量 */
  font-size: 1em;
  transition: all var(--transition-speed) ease; /* 使用变量 */
  background-color: #fcfdff;
}
.form-group input:focus {
  outline: none;
  border-color: var(--color-primary); /* 使用变量 */
  box-shadow: 0 0 0 3px var(--color-primary-light); /* 使用变量 */
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background-color: var(--color-primary); /* 使用变量 */
  color: white;
  border: none;
  border-radius: var(--border-radius); /* 使用变量 */
  cursor: pointer;
  font-size: 1.1em;
  font-weight: 600;
  transition: all var(--transition-speed) ease; /* 使用变量 */
  letter-spacing: 2px;
}
.submit-btn:hover {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.error-message {
  color: var(--color-danger); /* 使用变量 */
  margin-bottom: var(--spacing-md); /* 使用变量 */
  text-align: center;
  background-color: rgba(220, 53, 69, 0.1);
  padding: var(--spacing-sm); /* 使用变量 */
  border-radius: var(--border-radius); /* 使用变量 */
}
</style>