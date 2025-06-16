import { createRouter, createWebHistory } from 'vue-router';
import Login from '@/views/Login.vue';
import StudentDashboard from '@/views/StudentDashboard.vue'; // 确保引入的是 StudentDashboard
import StudentForm from '@/components/StudentForm.vue';
import DashboardHome from '@/views/DashboardHome.vue';
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    redirect: '/students'
  },
  {
    path: '/',
    redirect: '/dashboard' // 重定向到新的主页
  },
  {
    path: '/dashboard', // 新增主页路由
    name: 'DashboardHome',
    component: DashboardHome,
    meta: { requiresAuth: true }
  },
  {
    path: '/students',
    name: 'StudentDashboard', // 路由名称
    component: StudentDashboard, // 路由组件
    meta: { requiresAuth: true }
  },
  {
    path: '/students/new',
    name: 'StudentAdd',
    component: StudentForm,
    meta: { requiresAuth: true }
  },
  {
    path: '/students/edit/:id',
    name: 'StudentEdit',
    component: StudentForm,
    props: true,
    meta: { requiresAuth: true }
  },
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