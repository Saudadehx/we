// 文件路径: frontend-student-management/src/router/index.js
import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import Login from '@/views/Login.vue';

const routes = [
  { path: '/login', name: 'Login', component: Login },
  { path: '/', redirect: '/login' },

  // --- 管理员路由 ---
  { path: '/dashboard/home', name: 'AdminHome', component: () => import('@/views/DashboardHome.vue'), meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/dashboard/students', name: 'AdminStudentMgmt', component: () => import('@/views/StudentDashboard.vue'), meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/dashboard/courses', name: 'AdminCourseMgmt', component: () => import('@/views/admin/CourseManagement.vue'), meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/dashboard/teachers', name: 'AdminTeacherMgmt', component: () => import('@/views/admin/TeacherManagement.vue'), meta: { requiresAuth: true, roles: ['ADMIN'] } },
  // 【新增】管理员设置路由
  { path: '/dashboard/settings', name: 'AdminSettings', component: () => import('@/views/admin/AdminSettings.vue'), meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/dashboard/majors', name: 'AdminMajorMgmt', component: () => import('@/views/admin/MajorManagement.vue'), meta: { requiresAuth: true, roles: ['ADMIN'] } },
  { path: '/dashboard/classes', name: 'AdminClassMgmt', component: () => import('@/views/admin/ClassManagement.vue'), meta: { requiresAuth: true, roles: ['ADMIN'] } },

  // --- 教师路由 ---
  { path: '/teacher/dashboard', name: 'TeacherDashboard', component: () => import('@/views/teacher/TeacherDashboard.vue'), meta: { requiresAuth: true, roles: ['TEACHER'] } },
  { path: '/teacher/course/:id/grades', name: 'GradeEntry', component: () => import('@/views/teacher/GradeEntry.vue'), props: true, meta: { requiresAuth: true, roles: ['TEACHER'] } },


  { path: '/student/profile', name: 'StudentProfile', component: () => import('@/views/MyProfile.vue'), meta: { requiresAuth: true, roles: ['STUDENT'] } },
  { path: '/student/available-courses', name: 'StudentAvailableCourses', component: () => import('@/views/student/AvailableCourses.vue'), meta: { requiresAuth: true, roles: ['STUDENT'] } },

  { path: '/student/schedule', name: 'StudentSchedule', component: () => import('@/views/student/MySchedule.vue'), meta: { requiresAuth: true, roles: ['STUDENT'] } },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

// ... router.beforeEach 保持不变 ...
const roleRedirectMap = {
  'ADMIN': 'AdminHome',
  'TEACHER': 'TeacherDashboard',
  'STUDENT': 'StudentProfile'
};
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  const isLoggedIn = authStore.isLoggedIn;
  const userRole = authStore.userRole;

  if (to.meta.requiresAuth) {
    if (!isLoggedIn) {
      next({ name: 'Login' });
    } else {
      if (to.meta.roles && !to.meta.roles.includes(userRole)) {
        const redirectRouteName = roleRedirectMap[userRole];
        if (redirectRouteName) {
          next({ name: redirectRouteName });
        } else {
          next({ name: 'Login' });
        }
      } else {
        next();
      }
    }
  } else {
    next();
  }
});


export default router;