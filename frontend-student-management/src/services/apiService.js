import axios from 'axios';

// apiClient 的创建和拦截器保持不变
export const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' }
});

apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, error => {
  return Promise.reject(error);
});

apiClient.interceptors.response.use(
    response => {
      const res = response.data;
      if (res.success) {
        return res.data;
      }
      const error = new Error(res.message || 'Error');
      error.code = res.code;
      return Promise.reject(error);
    },
    error => {
      console.error('API Error:', error);
      return Promise.reject(error);
    }
);

// 学生管理相关的API保持不变
export const studentService = {
    getStats() { return apiClient.get('/students/stats'); },
    getAllStudents() { return apiClient.get('/students'); },
    getStudentById(id) { return apiClient.get(`/students/${id}`); },
    createStudent(studentData) { return apiClient.post('/students', studentData); },
    updateStudent(id, studentData) { return apiClient.put(`/students/${id}`, studentData); },
    deleteStudent(id) { return apiClient.delete(`/students/${id}`); },
    getMyProfile() { return apiClient.get('/my-profile'); },
    updateMyProfile(profileData) { return apiClient.put('/my-profile', profileData); }

};

// 注意：authService 和对 jwt-decode, authStore 的导入都已移除
// 因为相关逻辑已经迁移到 stores/auth.js 中