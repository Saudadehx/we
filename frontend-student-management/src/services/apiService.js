import axios from 'axios';

// ... (apiClient 和拦截器保持不变) ...
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
        if (response.data.success) {
            return response.data.data;
        } else {
            return Promise.reject(new Error(response.data.message || '操作失败，未知错误。'));
        }
    },
    error => {
        let errorMessage = '网络错误，请稍后重试。';
        if (error.response) {
            if (error.response.data && error.response.data.message) {
                errorMessage = error.response.data.message;
            } else if (error.response.status) {
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
    // 课程目录 (Catalog)
    getAllCatalogs: () => apiClient.get('/course-catalogs'),
    createCatalog: (catalogData) => apiClient.post('/course-catalogs', catalogData),
    // ✨ 新增
    updateCatalog: (id, catalogData) => apiClient.put(`/course-catalogs/${id}`, catalogData),
    deleteCatalog: (id) => apiClient.delete(`/course-catalogs/${id}`),

    // 课程安排 (Offering)
    getAllOfferings: () => apiClient.get('/course-offerings'),
    createOffering: (offeringData) => apiClient.post('/course-offerings', offeringData),
    updateOffering: (id, offeringData) => apiClient.put(`/course-offerings/${id}`, offeringData),
    deleteOffering: (id) => apiClient.delete(`/course-offerings/${id}`),
};

export const enrollmentService = {
    // 教师用API, URL中的courseId现在是offeringId
    getForCourse: (offeringId) => apiClient.get(`/teachers/me/courses/${offeringId}/enrollments`),
    updateGrade: (enrollmentId, score) => apiClient.put(`/enrollments/${enrollmentId}`, { score }),

    // 学生用API, URL中的courseId现在是offeringId
    enrollInCourse: (offeringId) => apiClient.post('/students/me/enrollments', { courseId: offeringId }),
    dropCourse: (enrollmentId) => apiClient.delete(`/students/me/enrollments/${enrollmentId}`),
    getAvailableOfferings: () => apiClient.get('/students/me/available-courses'), // 后端返回 List<CourseOffering>

    // 管理员用API
    createEnrollment: (data) => apiClient.post('/enrollments', data),
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