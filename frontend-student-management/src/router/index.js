import { createRouter, createWebHistory } from 'vue-router';
import StudentList from '@/components/StudentList.vue';
import StudentForm from '@/components/StudentForm.vue';
import Login from '@/views/Login.vue'; // 引入我们新建的登录组件

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    redirect: '/students' // 根路径重定向到学生列表
  },
  {
    path: '/students',
    name: 'StudentList',
    component: StudentList,
    meta: { requiresAuth: true } // 添加一个元信息，表示这个路由需要认证
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

// --- 全局路由守卫 ---
router.beforeEach((to, from, next) => {
  const loggedIn = localStorage.getItem('token');

  // 如果目标路由需要认证
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!loggedIn) {
      // 如果用户未登录，则重定向到登录页
      next({ name: 'Login' });
    } else {
      // 如果用户已登录，则放行
      next();
    }
  } else {
    // 如果目标路由不需要认证（比如登录页本身），则直接放行
    next();
  }
});

export default router;