import { createRouter, createWebHistory } from 'vue-router';
import Login from '@/views/Login.vue';
import StudentDashboard from '@/views/StudentDashboard.vue'; // 确保引入的是 StudentDashboard
import DashboardHome from '@/views/DashboardHome.vue';

const routes = [
  { path: '/login', name: 'Login', component: Login },
  { path: '/', redirect: '/dashboard' },
  {
    path: '/dashboard',
    name: 'DashboardHome',
    component: DashboardHome,
    meta: { requiresAuth: true }
  },
  {
    path: '/students',
    name: 'StudentDashboard',
    component: StudentDashboard,
    meta: { requiresAuth: true }
  },
  // ✨ 修正点：确保这里没有 /students/new 和 /students/edit/:id 的路由了
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

// 全局路由守卫 (保持不变)
router.beforeEach((to, from, next) => {
  const loggedIn = localStorage.getItem('token');
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!loggedIn) {
      next({ name: 'Login' });
    } else {
      next();
    }
  } else {
    next();
  }
});

export default router;