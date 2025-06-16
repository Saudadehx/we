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
          <input type="text" id="username" v-model="username" required placeholder="请输入用户名">
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
import { useRouter } from 'vue-router';
import { authService } from '@/services/apiService';

const username = ref('');
const password = ref('');
const errorMessage = ref('');
const router = useRouter();

const handleLogin = async () => {
  errorMessage.value = '';
  try {
    await authService.login(username.value, password.value);
    await router.push('/dashboard');
  } catch (error) {
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
  /* 添加一个优雅的背景渐变 */
  background: linear-gradient(135deg, #ece9e6, #ffffff);
}

.login-card {
  width: 400px;
  padding: 40px;
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  animation: slideUp 0.6s ease-out;
}
@keyframes slideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.card-header {
  text-align: center;
  margin-bottom: 2rem;
}
.card-header h2 {
  margin: 0;
  font-size: 2em;
  font-weight: 700;
  color: #2c3e50;
}
.card-header p {
  margin-top: 0.5rem;
  color: #7f8c8d;
}

.form-group {
  margin-bottom: 1.5rem;
}
.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #555;
}

.form-group input {
  width: 100%;
  padding: 12px 15px;
  box-sizing: border-box;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1em;
  transition: border-color 0.3s, box-shadow 0.3s;
}
.form-group input:focus {
  outline: none;
  border-color: #337ab7;
  box-shadow: 0 0 0 3px rgba(51, 122, 183, 0.1);
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #3a7bd5, #3a60d5);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1.1em;
  font-weight: 600;
  transition: all 0.3s;
  letter-spacing: 2px;
}
.submit-btn:hover {
  opacity: 0.9;
  box-shadow: 0 4px 15px rgba(58, 123, 213, 0.4);
}

.error-message {
  color: #e74c3c;
  margin-bottom: 1rem;
  text-align: center;
  background-color: rgba(231, 76, 60, 0.1);
  padding: 10px;
  border-radius: 6px;
}
</style>