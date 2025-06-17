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

  // --- 教师路由 ---
  { path: '/teacher/dashboard', name: 'TeacherDashboard', component: () => import('@/views/teacher/TeacherDashboard.vue'), meta: { requiresAuth: true, roles: ['TEACHER'] } },
  { path: '/teacher/course/:id/grades', name: 'GradeEntry', component: () => import('@/views/teacher/GradeEntry.vue'), props: true, meta: { requiresAuth: true, roles: ['TEACHER'] } },

  // --- 学生路由 ---
  { path: '/student/profile', name: 'StudentProfile', component: () => import('@/views/MyProfile.vue'), meta: { requiresAuth: true, roles: ['STUDENT'] } },
  { path: '/student/courses', name: 'StudentCourses', component: () => import('@/views/student/MyCourses.vue'), meta: { requiresAuth: true, roles: ['STUDENT'] } },
  { path: '/student/available-courses', name: 'StudentAvailableCourses', component: () => import('@/views/student/AvailableCourses.vue'), meta: { requiresAuth: true, roles: ['STUDENT'] } },
  { path: '/student/timetable', name: 'StudentTimetable', component: () => import('@/views/student/MyTimetable.vue'), meta: { requiresAuth: true, roles: ['STUDENT'] } },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

// 定义角色与对应主页路由的映射
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
      // 未登录，重定向到登录页
      next({ name: 'Login' });
    } else {
      // 已登录
      if (to.meta.roles && !to.meta.roles.includes(userRole)) {
        // 角色不匹配，重定向到该角色对应的主页，如果没有对应主页则回登录页
        const redirectRouteName = roleRedirectMap[userRole];
        if (redirectRouteName) {
          next({ name: redirectRouteName });
        } else {
          next({ name: 'Login' });
        }
      } else {
        // 权限匹配，放行
        next();
      }
    }
  } else {
    // 不需要认证的路径，直接放行
    next();
  }
});

export default router;