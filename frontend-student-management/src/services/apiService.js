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
    response => {
        // 如果后端返回的 ApiResult 成功，则返回其数据部分
        if (response.data.success) {
            return response.data.data;
        } else {
            // 如果 ApiResult 失败，则将其 message 作为错误信息抛出
            return Promise.reject(new Error(response.data.message || '操作失败，未知错误。'));
        }
    },
    error => {
        // 对于非 ApiResult 结构的HTTP错误（例如网络错误，500错误等）
        // 提取后端返回的具体错误信息，或提供通用错误消息
        let errorMessage = '网络错误，请稍后重试。';
        if (error.response) {
            // 后端有响应，但状态码非2xx
            if (error.response.data && error.response.data.message) {
                // 如果后端返回了 ApiResult 格式的错误信息
                errorMessage = error.response.data.message;
            } else if (error.response.status) {
                // 根据HTTP状态码提供通用错误信息
                errorMessage = `请求失败：状态码 ${error.response.status}`;
                if (error.response.status === 401 || error.response.status === 403) {
                    errorMessage = '认证失败或无权限访问，请重新登录。';
                } else if (error.response.status === 404) {
                    errorMessage = '请求的资源不存在。';
                } else if (error.response.status === 409) {
                    errorMessage = '操作冲突，请检查输入。';
                } else if (error.response.status >= 500) {
                    errorMessage = '服务器内部错误，请联系管理员。';
                }
            }
        } else if (error.request) {
            // 请求已发出但未收到响应 (例如，网络连接中断)
            errorMessage = '无法连接到服务器，请检查您的网络。';
        }

        return Promise.reject(new Error(errorMessage));
    }
);

export const teacherService = {
    getAll: () => apiClient.get('/teachers'),
    create: (teacherData) => apiClient.post('/teachers', teacherData),
    update: (id, teacherData) => apiClient.put(`/teachers/${id}`, teacherData),
    delete: (id) => apiClient.delete(`/teachers/${id}`),
    getMyCourses: () => apiClient.get('/teachers/me/courses'),
};


export const majorService = {
    getAll: () => apiClient.get('/majors'),
    create: (majorData) => apiClient.post('/majors', majorData),
    update: (id, majorData) => apiClient.put(`/majors/${id}`, majorData),
    delete: (id) => apiClient.delete(`/majors/${id}`),
};

export const courseService = {
    getAll: (params = {}) => apiClient.get('/courses', { params }),
    create: (courseData) => apiClient.post('/courses', courseData),
    update: (id, courseData) => apiClient.put(`/courses/${id}`, courseData),
    delete: (id) => apiClient.delete(`/courses/${id}`),
};

export const enrollmentService = {
    getForCourse: (courseId) => apiClient.get(`/teachers/me/courses/${courseId}/enrollments`),
    updateGrade: (enrollmentId, score) => apiClient.put(`/enrollments/${enrollmentId}`, { score }),
    enrollInCourse: (courseId) => apiClient.post('/students/me/enrollments', { courseId }),
    dropCourse: (enrollmentId) => apiClient.delete(`/students/me/enrollments/${enrollmentId}`),
    assignCompulsoryCourses: (majorId, academicYear, semester) => apiClient.post('/enrollments/assign-compulsory', { majorId, academicYear, semester }),
    // 【新增】调用学生可选课程列表的接口
    getAvailableCourses: () => apiClient.get('/students/me/available-courses'),
};

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