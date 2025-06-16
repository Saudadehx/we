<template>
  <div class="page-container">
    <header class="page-header">
      <h1>课程管理</h1>
      <button @click="openAddModal" class="add-btn">新增课程</button>
    </header>

    <div class="content-card">
      <table class="data-table">
        <thead>
        <tr>
          <th>课程编号</th>
          <th>课程名称</th>
          <th>学分</th>
          <th>授课教师</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="isLoading">
          <td colspan="5" style="text-align: center; padding: 20px;">正在加载数据...</td>
        </tr>
        <tr v-for="course in courses" :key="course.id">
          <td>{{ course.courseId }}</td>
          <td>{{ course.courseName }}</td>
          <td>{{ course.credits }}</td>
          <td>{{ course.teacherName }}</td>
          <td>
            <button class="action-btn edit">编辑</button>
            <button class="action-btn delete">删除</button>
          </td>
        </tr>
        <tr v-if="!isLoading && courses.length === 0">
          <td colspan="5" style="text-align: center; padding: 20px;">暂无课程数据，请新增。</td>
        </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showAddModal" class="modal-overlay" @click.self="showAddModal = false">
      <div class="modal-content">
        <h2>新增课程</h2>
        <form @submit.prevent="handleAddCourse">
          <div class="form-group">
            <label for="courseId">课程编号</label>
            <input v-model="newCourse.courseId" id="courseId" required placeholder="例如：CS101">
          </div>
          <div class="form-group">
            <label for="courseName">课程名称</label>
            <input v-model="newCourse.courseName" id="courseName" required>
          </div>
          <div class="form-group">
            <label for="credits">学分</label>
            <input type="number" step="0.5" v-model="newCourse.credits" id="credits" required>
          </div>
          <div class="form-group">
            <label for="teacherId">授课教师</label>
            <select v-model="newCourse.teacherId" id="teacherId" required>
              <option disabled value="">请选择一位教师</option>
              <option v-for="teacher in teachers" :key="teacher.teacherId" :value="teacher.teacherId">
                {{ teacher.name }} ({{ teacher.teacherId }})
              </option>
            </select>
          </div>
          <div class="modal-actions">
            <button type="button" @click="showAddModal = false">取消</button>
            <button type="submit" :disabled="isSubmitting">
              {{ isSubmitting ? '创建中...' : '创建课程' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { courseService, teacherService } from '@/services/apiService';
import { showNotification } from '@/services/notificationStore';

const courses = ref([]);
const teachers = ref([]);
const isLoading = ref(true);
const isSubmitting = ref(false);
const showAddModal = ref(false);

const newCourse = ref({
  courseId: '',
  courseName: '',
  credits: null,
  teacherId: ''
});

const fetchCourses = async () => {
  isLoading.value = true;
  try {
    courses.value = await courseService.getAll();
  } catch (error) {
    showNotification(error.message || '获取课程列表失败', 'error');
  } finally {
    isLoading.value = false;
  }
};

const fetchTeachers = async () => {
  try {
    teachers.value = await teacherService.getAll();
  } catch (error) {
    showNotification('无法加载教师列表，请稍后重试', 'error');
  }
};

const openAddModal = () => {
  // 每次打开弹窗时都尝试获取最新的教师列表
  fetchTeachers();
  showAddModal.value = true;
};

const handleAddCourse = async () => {
  if (isSubmitting.value) return;
  isSubmitting.value = true;

  try {
    await courseService.create(newCourse.value);
    showNotification('课程创建成功！', 'success');
    showAddModal.value = false;
    newCourse.value = { courseId: '', courseName: '', credits: null, teacherId: '' };
    fetchCourses();
  } catch (error) {
    showNotification(error.message || '创建失败', 'error');
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(fetchCourses);
</script>

<style scoped>
@import '@/assets/styles/common-page.css';
@import '@/assets/styles/common-modal.css';

select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
  background-color: #fff;
}
</style>