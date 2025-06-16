<template>
  <div class="page-container">
    <header class="page-header">
      <h1>我的课程与成绩</h1>
    </header>

    <div v-if="isLoading" class="loading-indicator">正在查询您的成绩...</div>

    <div v-else class="content-card">
      <table class="data-table">
        <thead>
        <tr>
          <th>课程编号</th> <th>课程名称</th>
          <th>学分</th>   <th class="score-col">我的成绩</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="item in myEnrollments" :key="item.enrollmentId">
          <td>{{ item.courseId }}</td> <td>{{ item.courseName }}</td>
          <td>{{ item.credits }}</td>  <td class="score-col">
            <span :class="getScoreClass(item.score)">
              {{ item.score !== null ? item.score : '暂无' }}
            </span>
        </td>
        </tr>
        <tr v-if="myEnrollments.length === 0">
          <td colspan="4" style="text-align: center;">您尚未选修任何课程。</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { studentService } from '@/services/apiService';
import { showNotification } from '@/services/notificationStore';

const myEnrollments = ref([]);
const isLoading = ref(true);


onMounted(async () => {
  isLoading.value = true;
  try {
    // 使用我们刚刚在 apiService.js 中定义的、正确的函数名
    myEnrollments.value = await studentService.getMyCoursesAndGrades();
  } catch (error) {
    showNotification(error.message || '获取成绩失败', 'error');
  } finally {
    isLoading.value = false;
  }
});

const getScoreClass = (score) => {
  if (score === null) return 'score-pending';
  if (score >= 60) return 'score-pass';
  return 'score-fail';
}
</script>

<style scoped>
@import '@/assets/styles/common-page.css';

.score-col {
  text-align: center;
  font-weight: bold;
  font-size: 1.1em;
}
.score-pass {
  color: #28a745; /* 成功/通过的颜色 */
}
.score-fail {
  color: var(--color-danger); /* 危险/失败的颜色 */
}
.score-pending {
  color: var(--color-text-secondary);
  font-weight: normal;
  font-style: italic;
}
</style>