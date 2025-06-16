import axios from 'axios';
import { jwtDecode } from 'jwt-decode';

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
      // 登录成功后，将token存入localStorage
      localStorage.setItem('token', token);
    }
    return response.data;
  },

  logout() {
    // 登出时，从localStorage移除token
    localStorage.removeItem('token');
  },

  getUserRole() {
    const token = localStorage.getItem('token');
    if (!token) {
      return null;
    }
    try {
      const decodedToken = jwtDecode(token);
      // Spring Security默认的角色信息在 'authorities' 或 'roles' 字段里
      // 假设我们的后端在JWT中放入了角色信息
      // 注意：实际项目中，角色字段名需要和后端生成JWT时放入的字段名一致
      // Spring Security默认的角色会带 "ROLE_" 前缀
      if (decodedToken.authorities && decodedToken.authorities.includes('ROLE_ADMIN')) {
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