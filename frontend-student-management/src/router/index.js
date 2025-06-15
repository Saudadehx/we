import { createRouter, createWebHistory } from 'vue-router';
import StudentList from '@/components/StudentList.vue';
import StudentForm from '@/components/StudentForm.vue';

// Example: How you might structure if components were treated as views
// import StudentListView from '@/views/StudentListView.vue'; // Assuming you create this
// import StudentFormView from '@/views/StudentFormView.vue'; // Assuming you create this

const routes = [
  {
    path: '/',
    name: 'Home',
    component: StudentList
  },
  {
    path: '/students', // Explicit path for listing students
    name: 'StudentList',
    component: StudentList
  },
  {
    path: '/students/new',
    name: 'StudentAdd',
    component: StudentForm
  },
  {
    path: '/students/edit/:id',
    name: 'StudentEdit',
    component: StudentForm,
    props: true // Pass route params as props to the component
  },
  // Example of using components from a 'views' directory (if you choose to use that structure)
  // {
  //   path: '/view/students',
  //   name: 'StudentListView',
  //   component: () => import('@/views/StudentListView.vue') // Lazy loading example
  // },
  // {
  //   path: '/view/students/new',
  //   name: 'StudentAddView',
  //   component: () => import('@/views/StudentFormView.vue')
  // },
  // {
  //   path: '/view/students/edit/:id',
  //   name: 'StudentEditView',
  //   component: () => import('@/views/StudentFormView.vue'),
  //   props: true
  // }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

export default router;
