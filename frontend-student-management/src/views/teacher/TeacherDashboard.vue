<template>
  <div class="page-container">
    <header class="page-header">
      <h1>我的课程</h1>
      <p>选择一门课程，为学生录入成绩。</p>
    </header>

    <div v-if="isLoading" class="loading-indicator">正在加载您的课程数据...</div>

    <div v-else class="content-card">
      <table class="data-table teacher-course-table">
        <thead>
        <tr>
          <th>课程编号</th>
          <th>课程名称</th>
          <th>学分</th>
          <th>学年</th>
          <th>学期</th>
          <th style="text-align: right;">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="courses.length === 0 && !isLoading">
          <td colspan="6" class="no-data">
            <p>系统暂未给您分配任何课程。</p>
          </td>
        </tr>
        <tr v-for="course in courses" :key="course.id" class="course-row">
          <td>{{ course.courseCode }}</td>
          <td>{{ course.courseName }}</td>
          <td>{{ course.credits }}</td>
          <td>{{ course.academicYear }} - {{ course.academicYear + 1 }}</td>
          <td>第 {{ course.semester }} 学期</td>
          <td style="text-align: right;">
            <button @click="goToGradeEntry(course.id)" class="action-btn grade-entry-btn">
              进入成绩录入
            </button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { teacherService } from '@/services/apiService';
import { showNotification } from '@/services/notificationStore';

const courses = ref([]);
const isLoading = ref(true);
const router = useRouter();

const fetchMyCourses = async () => {
  isLoading.value = true;
  try {
    courses.value = await teacherService.getMyCourses();
  } catch (error) {
    showNotification(error.message || '获取课程列表失败', 'error');
  } finally {
    isLoading.value = false;
  }
};

const goToGradeEntry = (courseOfferingId) => {
  router.push({ name: 'GradeEntry', params: { id: courseOfferingId } });
};

onMounted(fetchMyCourses);
</script>

<style scoped>
@import '@/assets/styles/common-page.css';

.loading-indicator, .no-data {
  text-align: center;
  padding: 40px;
  font-size: 1.2em;
  color: var(--color-text-secondary);
}

.teacher-course-table .course-row {
  transition: background-color 0.2s ease;
}

.teacher-course-table .course-row:hover {
  background-color: var(--color-primary-light);
}

.grade-entry-btn {
  background-color: var(--color-primary);
  color: white;
  border: none;
  font-weight: 500;
  padding: 8px 16px;
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: opacity 0.2s;
}

.grade-entry-btn:hover {
  opacity: 0.9;
}
</style>