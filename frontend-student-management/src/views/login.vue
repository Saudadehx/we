<template>
  <div class="login-container">
    <div class="login-form">
      <h2>学生管理系统登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">用户名:</label>
          <input type="text" id="username" v-model="username" required>
        </div>
        <div class="form-group">
          <label for="password">密码:</label>
          <input type="password" id="password" v-model="password" required>
        </div>
        <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
        <button type="submit" class="submit-btn">登录</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { authService } from '@/services/apiService'; // 我们将在下一步创建它

const username = ref('');
const password = ref('');
const errorMessage = ref('');
const router = useRouter();

const handleLogin = async () => {
  errorMessage.value = '';
  try {
    await authService.login(username.value, password.value);
    // 登录成功后，跳转到学生列表页
    await router.push('/students');
  } catch (error) {
    console.error('登录失败:', error);
    errorMessage.value = '登录失败，请检查用户名或密码。';
  }
};
</script>

<style scoped>
.login-container {
  /* 确保容器占满其父元素（即 .app-main）的全部空间 */
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
}
.login-form {
  padding: 40px;
  border: 1px solid #ccc;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  width: 350px;
  background-color: white; /* 添加背景色以区分 */
}
h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #333; /* 调整字体颜色 */
}
.form-group {
  margin-bottom: 15px;
}
label {
  display: block;
  margin-bottom: 5px;
  color: #555; /* 调整字体颜色 */
}
input {
  width: 100%;
  padding: 10px;
  box-sizing: border-box;
  border: 1px solid #ddd;
  border-radius: 4px;
}
.submit-btn {
  width: 100%;
  padding: 10px;
  background-color: #337ab7;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}
.submit-btn:hover {
  background-color: #286090;
}
.error-message {
  color: red;
  margin-bottom: 15px;
  text-align: center;
}
</style>