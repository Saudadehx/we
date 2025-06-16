import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth'; // 1. 导入 auth store
import Login from '@/views/Login.vue';
import StudentDashboard from '@/views/StudentDashboard.vue';
import DashboardHome from '@/views/DashboardHome.vue';

const routes = [
  { path: '/login', name: 'Login', component: Login },
  { path: '/', redirect: '/dashboard' },
  {
    path: '/dashboard',
    name: 'DashboardHome',
    component: DashboardHome,
    meta: { requiresAuth: true, roles: ['ADMIN'] } // 2. 指定此页面只允许 ADMIN 访问
  },
  {
    path: '/students',
    name: 'StudentDashboard',
    component: StudentDashboard,
    meta: { requiresAuth: true, roles: ['ADMIN'] } // 2. 指定此页面只允许 ADMIN 访问
  },
  {
    path: '/my-profile',
    name: 'MyProfile',
    component: () => import('@/views/MyProfile.vue'),
    meta: { requiresAuth: true, roles: ['STUDENT'] } // 2. 指定此页面只允许 STUDENT 访问
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

// 3. 【核心修改】升级全局路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  const isLoggedIn = authStore.isLoggedIn;

  // 检查路由是否需要认证
  if (to.meta.requiresAuth) {
    if (!isLoggedIn) {
      // 如果需要认证但未登录，跳转到登录页
      next({ name: 'Login' });
    } else {
      // 如果已登录，检查角色权限
      const userRole = authStore.userRole; // 'ADMIN' 或 'STUDENT'
      if (to.meta.roles && !to.meta.roles.includes(userRole)) {
        // 如果此页面需要特定角色，但当前用户角色不匹配
        // (例如，学生试图访问管理员页面)
        // 则不允许访问，可以跳转到其各自的主页
        if (userRole === 'ADMIN') {
          next({ name: 'DashboardHome' });
        } else if (userRole === 'STUDENT') {
          next({ name: 'MyProfile' });
        } else {
          next({ name: 'Login' });
        }
      } else {
        // 角色匹配，允许访问
        next();
      }
    }
  } else {
    // 不需要认证的页面（如登录页），直接放行
    next();
  }
});

export default router;