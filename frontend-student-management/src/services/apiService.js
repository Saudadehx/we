// Description: 课程服务类，负责课程目录和课程安排的管理
import axios from 'axios';
// 这里使用了axios库来处理HTTP请求
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


// 响应拦截器，使其能处理结构化的错误
apiClient.interceptors.response.use(
    response => {
        // 成功的响应，如果后端遵循了我们的ApiResult结构
        if (response.data && typeof response.data.success === 'boolean') {
            if (response.data.success) {
                return response.data.data; // 只返回data部分
            } else {
                // 对于 success: false 的情况，也构建一个包含详细信息的Error对象
                const error = new Error(response.data.message || '操作失败，未知错误。');
                error.data = response.data.data; // 把后端的错误数据附加到Error对象上
                return Promise.reject(error);
            }
        }
        // 对于不遵循ApiResult结构的响应，直接返回data
        return response.data;
    },
    error => {
        // 处理HTTP层面的错误 (4xx, 5xx)
        let customError = new Error('网络错误，请稍后重试。');

        if (error.response) {
            const backendResponse = error.response.data;
            // 优先使用后端返回的ApiResult中的信息
            customError.message = backendResponse?.message || `请求失败：状态码 ${error.response.status}`;
            customError.data = backendResponse?.data; // 关键：将后端的错误数据附加到Error对象上
            customError.status = error.response.status;

            if (error.response.status === 401 || error.response.status === 403) {
                customError.message = '认证失败或无权限访问，请重新登录。';
            }
        } else if (error.request) {
            customError.message = '无法连接到服务器，请检查您的网络。';
        }

        return Promise.reject(customError);
    }
);

export const teacherService = { // 教师相关的服务
    getAll: () => apiClient.get('/teachers'),
    create: (teacherData) => apiClient.post('/teachers', teacherData),
    update: (id, teacherData) => apiClient.put(`/teachers/${id}`, teacherData),
    delete: (id) => apiClient.delete(`/teachers/${id}`),
    getMyCourses: () => apiClient.get('/teachers/me/courses'),
};


export const majorService = {  // 专业相关的服务
    getAll: () => apiClient.get('/majors'),
    create: (majorData) => apiClient.post('/majors', majorData),
    update: (id, majorData) => apiClient.put(`/majors/${id}`, majorData),
    delete: (id) => apiClient.delete(`/majors/${id}`),
};

export const courseService = {  // 课程相关的服务
    getAllCatalogs: () => apiClient.get('/course-catalogs'),
    createCatalog: (catalogData) => apiClient.post('/course-catalogs', catalogData),
    updateCatalog: (id, catalogData) => apiClient.put(`/course-catalogs/${id}`, catalogData),
    deleteCatalog: (id) => apiClient.delete(`/course-catalogs/${id}`),
    getAllOfferings: () => apiClient.get('/course-offerings'),
    createOffering: (offeringData) => apiClient.post('/course-offerings', offeringData),
    updateOffering: (id, offeringData) => apiClient.put(`/course-offerings/${id}`, offeringData),
    deleteOffering: (id) => apiClient.delete(`/course-offerings/${id}`),
};

export const enrollmentService = { // 学生选课和教师评分相关的服务
    getForCourse: (offeringId) => apiClient.get(`/teachers/me/courses/${offeringId}/enrollments`),
    updateGrade: (enrollmentId, score) => apiClient.put(`/enrollments/${enrollmentId}`, { score }),
    enrollInCourse: (offeringId) => apiClient.post('/students/me/enrollments', { courseId: offeringId }),
    dropCourse: (enrollmentId) => apiClient.delete(`/students/me/enrollments/${enrollmentId}`),
    getAvailableOfferings: () => apiClient.get('/students/me/available-courses'),
    createEnrollment: (data) => apiClient.post('/enrollments', data),
};

export const studentService = { // 学生相关的服务
    getStats: () => apiClient.get('/students/stats'),
    getAllStudents: () => apiClient.get('/students'),
    getStudentById: (id) => apiClient.get(`/students/${id}`),
    createStudent: (studentData) => apiClient.post('/students', studentData),
    updateStudent: (id, studentData) => apiClient.put(`/students/${id}`, studentData),
    deleteStudent: (id) => apiClient.delete(`/students/${id}`),
    getMyProfile: () => apiClient.get('/my-profile'),
    updateMyProfile: (profileData) => apiClient.put('/my-profile', profileData),
    getMyCoursesAndGrades: () => apiClient.get('/students/me/enrollments')
};