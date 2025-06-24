// Description: 统一的API服务中心，管理所有与后端交互的请求。
import axios from 'axios';

// 创建并配置axios实例
export const apiClient = axios.create({
    baseURL: 'http://localhost:8080/api',
    headers: { 'Content-Type': 'application/json' }
});

// 请求拦截器：自动为每个请求附上认证Token
apiClient.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, error => Promise.reject(error));

// 响应拦截器：统一处理后端返回的数据结构和错误信息
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
            customError.data = backendResponse?.data;
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

// --- 服务定义区 ---

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
    getAllCatalogs: () => apiClient.get('/course-catalogs'),
    createCatalog: (catalogData) => apiClient.post('/course-catalogs', catalogData),
    updateCatalog: (id, catalogData) => apiClient.put(`/course-catalogs/${id}`, catalogData),
    deleteCatalog: (id) => apiClient.delete(`/course-catalogs/${id}`),
    getAllOfferings: () => apiClient.get('/course-offerings'),
    createOffering: (offeringData) => apiClient.post('/course-offerings', offeringData),
    updateOffering: (id, offeringData) => apiClient.put(`/course-offerings/${id}`, offeringData),
    deleteOffering: (id) => apiClient.delete(`/course-offerings/${id}`),
};

export const enrollmentService = {
    getForCourse: (offeringId) => apiClient.get(`/teachers/me/courses/${offeringId}/enrollments`),
    updateGrade: (enrollmentId, score) => apiClient.put(`/enrollments/${enrollmentId}`, { score }),
    enrollInCourse: (offeringId) => apiClient.post('/students/me/enrollments', { courseId: offeringId }),
    dropCourse: (enrollmentId) => apiClient.delete(`/students/me/enrollments/${enrollmentId}`),
    getAvailableOfferings: () => apiClient.get('/students/me/available-courses'),
    createEnrollment: (data) => apiClient.post('/enrollments', data),
};

export const classService = {
    getAll: () => apiClient.get('/classes'),
    create: (classData) => apiClient.post('/classes', classData),
    update: (id, classData) => apiClient.put(`/classes/${id}`, classData),
    delete: (id) => apiClient.delete(`/classes/${id}`),
};

export const classroomService = {
    getAll: () => apiClient.get('/classrooms'),
    create: (classroomData) => apiClient.post('/classrooms', classroomData),
    update: (id, classroomData) => apiClient.put(`/classrooms/${id}`, classroomData),
    delete: (id) => apiClient.delete(`/classrooms/${id}`),
};

export const schedulingService = {
    generateSchedule: () => apiClient.post('/scheduling/generate'),
};

export const studentService = {
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

// 【统一的】系统设置服务
export const systemSettingService = {
    getCourseSelectionStatus() {
        return apiClient.get('/settings/course-selection-status');
    },
    setCourseSelectionStatus(isOpen) {
        return apiClient.post('/settings/course-selection-status', { isOpen });
    },
    getSchedulingSettings() {
        return apiClient.get('/settings/scheduling');
    },
    updateSchedulingSettings(settings) {
        return apiClient.post('/settings/scheduling', settings);
    }
};