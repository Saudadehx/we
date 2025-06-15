import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Optional: Add request interceptor (e.g., for auth tokens)
// apiClient.interceptors.request.use(config => {
//   const token = localStorage.getItem('user-token');
//   if (token) {
//     config.headers.Authorization = `Bearer ${token}`;
//   }
//   return config;
// }, error => Promise.reject(error));

// Optional: Add response interceptor (e.g., for global error handling)
// apiClient.interceptors.response.use(response => response, error => {
//   if (error.response && error.response.status === 401) {
//     // Assuming router is available or imported separately for navigation
//     // router.push('/login');
//   }
//   return Promise.reject(error);
// });

export default {
  getAllStudents() {
    return apiClient.get('/students');
  },
  getStudentById(id) {
    return apiClient.get(`/students/${id}`);
  },
  getStudentByStudentId(studentId) {
    return apiClient.get(`/students/by-studentid/${studentId}`);
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
