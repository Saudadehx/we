<template>
  <div class="page-container">
    <header class="page-header">
      <h1>教师管理</h1>
      <button @click="openAddModal" class="add-btn">新增教师</button>
    </header>

    <div class="content-card">
      <table class="data-table">
        <thead>
        <tr>
          <th>ID</th>
          <th>教师工号</th>
          <th>姓名</th>
          <th>所授课程</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="teacher in teachers" :key="teacher.id">
          <td>{{ teacher.id }}</td>
          <td>{{ teacher.teacherId }}</td>
          <td>{{ teacher.name }}</td>
          <td>
            <span v-if="teacher.taughtCourses && teacher.taughtCourses.length > 0">
              {{ teacher.taughtCourses.join(', ') }}
            </span>
            <span v-else class="no-cours">暂未分配课程</span>
          </td>
          <td>
            <button @click="openEditModal(teacher)" class="action-btn edit">编辑</button>
            <button @click="handleDeleteTeacher(teacher.id)" class="action-btn delete">删除</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h2>{{ isEditing ? '编辑教师' : '新增教师' }}</h2>
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label for="teacherId">教师工号</label>
            <input v-model="editableTeacher.teacherId" id="teacherId" required>
          </div>
          <div class="form-group">
            <label for="name">姓名</label>
            <input v-model="editableTeacher.name" id="name" required>
          </div>
          <div class="form-group">
            <label for="password">密码</label>
            <input type="password" v-model="editableTeacher.password" id="password" :placeholder="isEditing ? '不修改请留空' : '请输入初始密码'" :required="!isEditing">
          </div>
          <div class="modal-actions">
            <button type="button" @click="closeModal">取消</button>
            <button type="submit" :disabled="isSubmitting">{{ isSubmitting ? '处理中...' : '提交' }}</button>
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
const showModal = ref(false);
const isEditing = ref(false);
const isSubmitting = ref(false);

const editableTeacher = ref({
  id: null,
  teacherId: '',
  name: '',
  password: ''
});

const fetchTeachers = async () => {
  try {
    // 后端返回的是 TeacherDetailDTO
    teachers.value = await teacherService.getAll();
  } catch (error) {
    showNotification('获取教师列表失败', 'error');
  }
};

onMounted(fetchTeachers);

const resetForm = () => {
  editableTeacher.value = { id: null, teacherId: '', name: '', password: '' };
};

const openAddModal = () => {
  resetForm();
  isEditing.value = false;
  showModal.value = true;
};

const openEditModal = (teacher) => {
  // 注意：我们只编辑教师基本信息，taughtCourses 是只读的
  editableTeacher.value = {
    id: teacher.id,
    teacherId: teacher.teacherId,
    name: teacher.name,
    password: '' // 密码字段留空
  };
  isEditing.value = true;
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
  resetForm();
};

const handleSubmit = async () => {
  isSubmitting.value = true;
  try {
    if (isEditing.value) {
      await teacherService.update(editableTeacher.value.id, editableTeacher.value);
      showNotification('教师信息更新成功！', 'success');
    } else {
      await teacherService.create(editableTeacher.value);
      showNotification('教师创建成功！', 'success');
    }
    closeModal();
    await fetchTeachers();
  } catch (error) {
    showNotification(error.message || '操作失败', 'error');
  } finally {
    isSubmitting.value = false;
  }
};

const handleDeleteTeacher = async (id) => {
  if (window.confirm('确定要删除这位教师吗？该教师所有课程的授课教师将被置空。')) {
    try {
      await teacherService.delete(id);
      showNotification('教师删除成功！', 'success');
      await fetchTeachers();
    } catch (error) {
      showNotification(error.message || '删除失败', 'error');
    }
  }
};
</script>

<style scoped>
@import '@/assets/styles/common-page.css';
@import '@/assets/styles/common-modal.css';

.no-cours {
  color: #999;
  font-style: italic;
}
</style>