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
}, error => Promise.reject(error));

apiClient.interceptors.response.use(
    response => response.data.success ? response.data.data : Promise.reject(response.data),
    error => Promise.reject(error.response?.data || error)
);

export const teacherService = {
    getAll: () => apiClient.get('/teachers'),
    create: (teacherData) => apiClient.post('/teachers', teacherData),
    // 【新增】更新教师
    update: (id, teacherData) => apiClient.put(`/teachers/${id}`, teacherData),
    // 【新增】删除教师
    delete: (id) => apiClient.delete(`/teachers/${id}`),
    getMyCourses: () => apiClient.get('/teachers/me/courses'),
};

// 文件路径: frontend-student-management/src/services/apiService.js
// ...
export const courseService = {
    // 【修改】接收一个参数对象，并将其作为URL查询参数
    getAll: (params = {}) => apiClient.get('/courses', { params }),
    create: (courseData) => apiClient.post('/courses', courseData),
    update: (id, courseData) => apiClient.put(`/courses/${id}`, courseData),
    delete: (id) => apiClient.delete(`/courses/${id}`),
};
// ...

export const enrollmentService = {
    getForCourse: (courseId) => apiClient.get(`/teachers/me/courses/${courseId}/enrollments`),
    updateGrade: (enrollmentId, score) => apiClient.put(`/enrollments/${enrollmentId}`, { score }),
    enrollInCourse: (courseId) => apiClient.post('/students/me/enrollments', { courseId }),
};

// 学生管理相关的API保持不变
export const studentService = {
    getStats() { return apiClient.get('/students/stats'); },
    getAllStudents() { return apiClient.get('/students'); },
    getStudentById(id) { return apiClient.get(`/students/${id}`); },
    createStudent(studentData) { return apiClient.post('/students', studentData); },
    updateStudent(id, studentData) { return apiClient.put(`/students/${id}`, studentData); },
    deleteStudent(id) { return apiClient.delete(`/students/${id}`); },
    getMyProfile() { return apiClient.get('/my-profile'); },
    updateMyProfile(profileData) { return apiClient.put('/my-profile', profileData); },
    getMyCoursesAndGrades: () => apiClient.get('/students/me/enrollments')
};