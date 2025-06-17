<template>
  <div class="page-container">
    <header class="page-header">
      <h1>我的课程</h1>
      <p>点击下方的课程卡片，为学生录入成绩。</p>
    </header>

    <div v-if="isLoading" class="loading-indicator">正在加载您的课程数据...</div>

    <div v-else class="courseCatalog-grid">
      <div v-for="courseCatalog in courses" :key="courseCatalog.id" class="courseCatalog-card" @click="goToGradeEntry(courseCatalog.id)">
        <div class="card-header">
          <span class="card-icon">&#128218;</span>
          <h3>{{ courseCatalog.courseName }}</h3>
        </div>
        <div class="card-body">
          <p><strong>课程编号:</strong> {{ courseCatalog.courseId }}</p>
          <p><strong>学分:</strong> {{ courseCatalog.credits }}</p>
        </div>
        <div class="card-footer">
          <span>进入成绩录入</span>
        </div>
      </div>
      <div v-if="courses.length === 0 && !isLoading" class="no-data">
        <p>系统暂未给您分配任何课程。</p>
      </div>
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
    // 后端需要一个能返回当前登录教师所授课程的API
    // 我们在 service 中假设这个 API 是 teacherService.getMyCourses()
    courses.value = await teacherService.getMyCourses();
  } catch (error) {
    showNotification(error.message || '获取课程列表失败', 'error');
  } finally {
    isLoading.value = false;
  }
};

// 点击课程卡片，跳转到对应的成绩录入页面
const goToGradeEntry = (courseId) => {
  router.push({ name: 'GradeEntry', params: { id: courseId } });
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

.courseCatalog-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.courseCatalog-card {
  background-color: #fff;
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 保证子元素圆角 */
}

.courseCatalog-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.12);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  background-color: var(--color-primary-light);
  padding: 16px;
}

.card-icon {
  font-size: 1.5em;
  color: var(--color-primary);
}

.card-header h3 {
  margin: 0;
  font-size: 1.2em;
  color: var(--color-text-primary);
}

.card-body {
  padding: 16px;
  flex-grow: 1;
}

.card-body p {
  margin: 0 0 8px 0;
  color: var(--color-text-secondary);
}
.card-body p strong {
  color: var(--color-text-primary);
}

.card-footer {
  text-align: center;
  padding: 12px;
  background-color: #f8f9fa;
  border-top: 1px solid var(--color-border);
  color: var(--color-primary);
  font-weight: 500;
}
</style>