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
          <th>上课时间</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="isLoading">
          <td colspan="6" style="text-align: center; padding: 20px;">正在加载数据...</td>
        </tr>
        <tr v-for="course in courses" :key="course.id">
          <td>{{ course.courseId }}</td>
          <td>{{ course.courseName }}</td>
          <td>{{ course.credits }}</td>
          <td>{{ course.teacherName }}</td>
          <td>{{ formatCourseTime(course.courseDay, course.courseTime) }}</td>
          <td>
            <button @click="openEditModal(course)" class="action-btn edit">编辑</button>
            <button @click="handleDeleteCourse(course.id)" class="action-btn delete">删除</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h2>{{ isEditing ? '编辑课程' : '新增课程' }}</h2>
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label for="courseId">课程编号</label>
            <input v-model="editableCourse.courseId" id="courseId" required>
          </div>
          <div class="form-group">
            <label for="courseName">课程名称</label>
            <input v-model="editableCourse.courseName" id="courseName" required>
          </div>
          <div class="form-group">
            <label for="credits">学分</label>
            <input type="number" step="0.5" v-model="editableCourse.credits" id="credits" required>
          </div>
          <div class="form-group">
            <label for="teacherId">授课教师</label>
            <select v-model="editableCourse.teacherId" id="teacherId" required>
              <option disabled value="">请选择一位教师</option>
              <option v-for="teacher in teachers" :key="teacher.teacherId" :value="teacher.teacherId">
                {{ teacher.name }} ({{ teacher.teacherId }})
              </option>
            </select>
          </div>
          <div class="form-group">
            <label for="courseDay">上课日</label>
            <select v-model="editableCourse.courseDay" id="courseDay">
              <option :value="null">未安排</option>
              <option v-for="day in 7" :key="day" :value="day">星期{{ '一二三四五六日'[day-1] }}</option>
            </select>
          </div>
          <div class="form-group">
            <label for="courseTime">上课时段</label>
            <select v-model="editableCourse.courseTime" id="courseTime">
              <option :value="null">未安排</option>
              <option v-for="time in 5" :key="time" :value="time">第 {{ time }} 大节</option>
            </select>
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
import { courseService, teacherService } from '@/services/apiService';
import { showNotification } from '@/services/notificationStore';

// ... data refs and other functions ...
const courses = ref([]);
const teachers = ref([]);
const isLoading = ref(true);
const isSubmitting = ref(false);
const showModal = ref(false);
const isEditing = ref(false);

const editableCourse = ref({
  id: null,
  courseId: '',
  courseName: '',
  credits: null,
  teacherId: '',
  courseDay: null,
  courseTime: null,
});

const fetchCourses = async () => { /* ... no change ... */ };
const fetchTeachers = async () => { /* ... no change ... */ };
onMounted(() => {
  fetchCourses();
  fetchTeachers();
});

// 【新增】格式化时间显示的辅助函数
const formatCourseTime = (day, time) => {
  if (!day || !time) return '未安排';
  const dayStr = '星期' + '一二三四五六日'[day-1];
  const timeStr = `第 ${time} 大节`;
  return `${dayStr} ${timeStr}`;
};

// ... resetForm, openAddModal, openEditModal, closeModal, handleSubmit, handleDeleteCourse ...
// 这些函数基本不变，只是需要确保在 resetForm 和 openEditModal 中处理新字段
const resetForm = () => {
  editableCourse.value = {
    id: null, courseId: '', courseName: '', credits: null, teacherId: '',
    courseDay: null, courseTime: null // 重置时间字段
  };
};

const openEditModal = (course) => {
  editableCourse.value = { ...course }; // 直接拷贝即可，因为DTO字段名一致
  isEditing.value = true;
  showModal.value = true;
};
// handleSubmit 里的 payload 需包含新字段
const handleSubmit = async () => {
  if (isSubmitting.value) return;
  isSubmitting.value = true;

  try {
    const payload = { ...editableCourse.value };
    if (isEditing.value) {
      await courseService.update(editableCourse.value.id, payload);
      showNotification('课程更新成功！', 'success');
    } else {
      await courseService.create(payload);
      showNotification('课程创建成功！', 'success');
    }
    closeModal();
    await fetchCourses();
  } catch (error) {
    showNotification(error.message || '操作失败', 'error');
  } finally {
    isSubmitting.value = false;
  }
};

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