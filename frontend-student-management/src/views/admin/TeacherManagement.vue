<template>
  <div class="page-container">
    <header class="page-header">
      <h1>教师管理</h1>
      <button @click="showAddModal = true" class="add-btn">新增教师</button>
    </header>

    <div class="content-card">
      <table class="data-table">
        <thead>
        <tr>
          <th>教师工号</th>
          <th>姓名</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="teacher in teachers" :key="teacher.teacherId">
          <td>{{ teacher.teacherId }}</td>
          <td>{{ teacher.name }}</td>
          <td>
            <button class="action-btn edit">编辑</button>
            <button class="action-btn delete">删除</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showAddModal" class="modal-overlay" @click.self="showAddModal = false">
      <div class="modal-content">
        <h2>新增教师</h2>
        <form @submit.prevent="handleAddTeacher">
          <div class="form-group">
            <label for="teacherId">教师工号</label>
            <input v-model="newTeacher.teacherId" id="teacherId" required>
          </div>
          <div class="form-group">
            <label for="name">姓名</label>
            <input v-model="newTeacher.name" id="name" required>
          </div>
          <div class="form-group">
            <label for="password">初始密码</label>
            <input type="password" v-model="newTeacher.password" id="password" required>
          </div>
          <div class="modal-actions">
            <button type="button" @click="showAddModal = false">取消</button>
            <button type="submit">创建</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { teacherService } from '@/services/apiService.js';
import { showNotification } from '@/services/notificationStore.js';

const teachers = ref([]);
const showAddModal = ref(false);
const newTeacher = ref({
  teacherId: '',
  name: '',
  password: ''
});

const fetchTeachers = async () => {
  try {
    teachers.value = await teacherService.getAll();
  } catch (error) {
    showNotification('获取教师列表失败', 'error');
  }
};

const handleAddTeacher = async () => {
  try {
    await teacherService.create(newTeacher.value);
    showNotification('教师创建成功！', 'success');
    showAddModal.value = false;
    newTeacher.value = { teacherId: '', name: '', password: '' }; // 重置表单
    fetchTeachers(); // 重新加载列表
  } catch (error) {
    showNotification(error.message || '创建失败', 'error');
  }
};

onMounted(fetchTeachers);
</script>

<style scoped>
/* 引入通用页面和弹窗样式 */
@import '@/assets/styles/common-page.css';
@import '@/assets/styles/common-modal.css';
</style>