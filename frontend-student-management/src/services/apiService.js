import axios from 'axios';
import { jwtDecode } from 'jwt-decode';
import { isLoggedIn } from './authStore.js';

// 创建一个axios实例
const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api', // API基础路径
  headers: {
    'Content-Type': 'application/json'
  }
});

// --- 请求拦截器 ---
// 在每个请求发送前，都会执行这个函数
apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    // 如果本地存储中有token，则在请求头中加入 Authorization
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, error => {
  return Promise.reject(error);
});


// 封装认证相关的API
export const authService = {
  async login(username, password) {
    const response = await apiClient.post('/auth/authenticate', { username, password });
    const token = response.data.token;
    if (token) {
      localStorage.setItem('token', token);
      isLoggedIn.value = true; // 登录后，更新状态为 true
    }
    return response.data;
  },

  logout() {
    localStorage.removeItem('token');
    isLoggedIn.value = false; // 登出后，更新状态为 false
  },

  getUserRole() {
    const token = localStorage.getItem('token');
    if (!token) {
      return null;
    }
    try {
      const decodedToken = jwtDecode(token);
      if (decodedToken.authorities && decodedToken.authorities.some(auth => auth.authority ==='ROLE_ADMIN')) {
        return 'ADMIN';
      }
      return 'STUDENT';
    } catch (error) {
      console.error("Token解码失败", error);
      this.logout();
      return null;
    }
  }
};

// 封装学生管理相关的API
export const studentService = {
  getStats() {
    return apiClient.get('/students/stats');
  },
  getAllStudents() {
    return apiClient.get('/students');
  },

  getStudentById(id) {
    return apiClient.get(`/students/${id}`);
  },

  createStudent(studentData) {
    return apiClient.post('/students', studentData);
  },

  updateStudent(id, studentData) {
    return apiClient.put(`/students/${id}`, studentData);
  },

  deleteStudent(id) {
    return apiClient.delete(`/students/${id}`);
  }
};