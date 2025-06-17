<template>
  <div class="page-container">
    <header class="page-header">
      <h1>我的课程与成绩</h1>
      <div class="summary-card">
        <div class="summary-item">
          <span class="summary-label">已修总学分:</span>
          <span class="summary-value">{{ totalCredits }}</span>
        </div>
      </div>
    </header>

    <div v-if="isLoading" class="loading-indicator">正在查询您的成绩...</div>

    <div v-else class="content-card">
      <table class="data-table my-courses-table">
        <thead>
        <tr>
          <th>课程编号</th>
          <th>课程名称</th>
          <th>学分</th>
          <th class="score-col">我的成绩</th>
          <th class="action-col">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="item in myEnrollments" :key="item.enrollmentId" class="table-row">
          <td>{{ item.courseId }}</td>
          <td>{{ item.courseName }}</td>
          <td>{{ item.credits }}</td>
          <td class="score-col">
            <span :class="getScoreClass(item.score)" class="score-display">
              <template v-if="item.score !== null">
                <svg v-if="item.score >= 60" class="score-icon success-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
                <svg v-else class="score-icon fail-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
                {{ item.score }}
              </template>
              <template v-else>
                 <svg class="score-icon pending-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                   <circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line>
                 </svg>
                 暂无
              </template>
            </span>
          </td>
          <td class="action-col">
            <button
                @click="handleDropCourse(item.enrollmentId)"
                class="action-btn delete drop-course-btn"
                :disabled="isDropping"
            >
              <span v-if="isDropping">处理中...</span>
              <span v-else>退课</span>
            </button>
          </td>
        </tr>
        <tr v-if="myEnrollments.length === 0">
          <td colspan="5" class="no-data-cell">
            <div class="no-data-content">
              <svg class="no-data-icon" xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="8" y1="12" x2="16" y2="12"></line>
              </svg>
              <p>您尚未选修任何课程。</p>
            </div>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { studentService, enrollmentService } from '@/services/apiService';
import { showNotification } from '@/services/notificationStore';

const myEnrollments = ref([]);
const isLoading = ref(true);
const isDropping = ref(false);

// 计算总学分
const totalCredits = computed(() => {
  return myEnrollments.value.reduce((sum, item) => sum + (item.credits || 0), 0);
});

// onMounted 统一使用一个函数来获取数据
const fetchMyCoursesAndGrades = async () => {
  isLoading.value = true;
  try {
    myEnrollments.value = await studentService.getMyCoursesAndGrades();
  } catch (error) {
    showNotification(error.message || '获取成绩失败', 'error');
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchMyCoursesAndGrades); // 页面加载时调用

const getScoreClass = (score) => {
  if (score === null) return 'score-pending';
  if (score >= 60) return 'score-pass';
  return 'score-fail';
}

const handleDropCourse = async (enrollmentId) => {
  if (!confirm('确定要退选这门课程吗？退选后将无法恢复。')) {
    return;
  }

  isDropping.value = true; // 开始退课，禁用按钮
  try {
    await enrollmentService.dropCourse(enrollmentId);
    showNotification('退课成功！', 'success');
    await fetchMyCoursesAndGrades(); // 退课成功后重新加载列表
  } catch (error) {
    showNotification(error.message || '退课失败，请重试', 'error');
  } finally {
    isDropping.value = false; // 恢复按钮状态
  }
};
</script>

<style scoped>
@import '@/assets/styles/common-page.css';

.page-header {
  display: flex;
  flex-direction: column; /* 让标题和统计卡片垂直排列 */
  align-items: flex-start; /* 左对齐 */
  margin-bottom: 24px;
}

.page-header h1 {
  margin-bottom: 15px; /* 增加标题和统计卡片的间距 */
}

.summary-card {
  background-color: var(--color-surface);
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
  padding: 20px 30px;
  display: flex;
  gap: 40px; /* 统计项之间的间距 */
  align-items: center;
  margin-top: 10px; /* 与标题的间距 */
  min-width: 300px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.summary-label {
  font-size: 0.9em;
  color: var(--color-text-secondary);
  margin-bottom: 5px;
}

.summary-value {
  font-size: 1.8em;
  font-weight: bold;
  color: var(--color-primary);
}


/* 表格样式美化 */
.my-courses-table {
  /* 增加表格圆角和阴影 */
  border-radius: var(--border-radius);
  overflow: hidden; /* 确保内容在圆角内 */
  box-shadow: var(--box-shadow);
}

.my-courses-table th, .my-courses-table td {
  padding: 14px 18px; /* 增加内边距 */
  vertical-align: middle;
}

.my-courses-table th {
  background-color: var(--color-background); /* 标题行使用更浅的背景色 */
  color: var(--color-text-primary);
  font-weight: 600;
  font-size: 0.95em;
  border-bottom: 1px solid var(--color-border);
}

.table-row {
  background-color: var(--color-surface);
  transition: background-color 0.2s ease;
}
.table-row:hover {
  background-color: var(--color-primary-light); /* 行hover效果 */
}
.table-row:last-child td {
  border-bottom: none; /* 最后一行无底部边框 */
}


.score-col {
  text-align: center;
  font-weight: bold;
  font-size: 1.1em;
  width: 150px; /* 成绩列固定宽度 */
}
.score-display {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.score-icon {
  width: 18px;
  height: 18px;
}
.score-pass {
  color: var(--color-success);
}
.score-pass .success-icon {
  color: var(--color-success);
}
.score-fail {
  color: var(--color-danger);
}
.score-fail .fail-icon {
  color: var(--color-danger);
}
.score-pending {
  color: var(--color-text-secondary);
  font-weight: normal;
  font-style: italic;
}
.score-pending .pending-icon {
  color: var(--color-text-secondary);
}

.action-col {
  text-align: center;
  width: 100px;
}

.drop-course-btn {
  background-color: #fdf2f2; /* 浅红背景 */
  color: var(--color-danger); /* 红色文字 */
  border: 1px solid rgba(220, 53, 69, 0.2); /* 红色边框 */
  padding: 8px 18px;
  font-size: 0.9em;
  font-weight: 500;
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}
.drop-course-btn:hover:not(:disabled) {
  background-color: var(--color-danger);
  color: white;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}
.drop-course-btn:disabled {
  background-color: #e0e0e0;
  color: #a0a0a0;
  cursor: not-allowed;
  opacity: 0.8;
  box-shadow: none;
}

/* 空数据提示 */
.no-data-cell {
  text-align: center;
  padding: 40px 20px;
  color: var(--color-text-secondary);
}
.no-data-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.no-data-icon {
  color: var(--color-border);
  opacity: 0.8;
}
.no-data-content p {
  margin: 0;
  font-size: 1.1em;
}
</style>