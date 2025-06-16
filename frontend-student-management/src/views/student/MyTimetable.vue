// 文件路径: frontend-student-management/src/views/student/MyTimetable.vue
<template>
  <div class="page-container">
    <header class="page-header">
      <h1>我的课表</h1>
      <p>以下是您本学期已选课程的时间安排。</p>
    </header>

    <div v-if="isLoading" class="loading-indicator">正在生成您的课表...</div>

    <div v-else class="content-card">
      <div class="timetable">
        <div class="header-cell">时间</div>
        <div v-for="day in days" :key="day" class="header-cell day-header">{{ day }}</div>

        <template v-for="timeSlot in timeSlots" :key="timeSlot.id">
          <div class="header-cell time-header">
            <div>第 {{ timeSlot.id }} 节</div>
            <div class="time-range">{{ timeSlot.range }}</div>
          </div>
          <div v-for="day in days.length" :key="`${timeSlot.id}-${day}`" class="class-cell">
            <div v-if="getCourseAt(day, timeSlot.id)" class="course-item">
              <strong>{{ getCourseAt(day, timeSlot.id).courseName }}</strong>
              <span>{{ getCourseAt(day, timeSlot.id).teacherName || 'N/A' }}</span>
              <span>学分: {{ getCourseAt(day, timeSlot.id).credits }}</span>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { studentService } from '@/services/apiService';
import { showNotification } from '@/services/notificationStore';

const isLoading = ref(true);
const myCourses = ref([]);

// 课表的时间和日期定义
const days = ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日'];
const timeSlots = [
  { id: 1, range: '8:00-9:40' },
  { id: 2, range: '10:00-11:40' },
  { id: 3, range: '14:00-15:40' },
  { id: 4, range: '16:00-17:40' },
  { id: 5, range: '19:00-20:40' }
];

onMounted(async () => {
  try {
    myCourses.value = await studentService.getMyCoursesAndGrades();
  } catch (error) {
    showNotification('加载课表数据失败', 'error');
  } finally {
    isLoading.value = false;
  }
});

// 使用 computed 属性创建一个易于查找的课程地图
const courseMap = computed(() => {
  const map = new Map();
  myCourses.value.forEach(course => {
    if (course.courseDay && course.courseTime) {
      const key = `${course.courseDay}-${course.courseTime}`;
      map.set(key, course);
    }
  });
  return map;
});

// 根据星期和节次获取课程
const getCourseAt = (day, time) => {
  const key = `${day}-${time}`;
  return courseMap.value.get(key);
};
</script>

<style scoped>
@import '@/assets/styles/common-page.css';

.timetable {
  display: grid;
  grid-template-columns: 100px repeat(7, 1fr); /* 1列时间头 + 7列日期 */
  gap: 4px;
  background-color: #e9ecef;
  border: 1px solid #dee2e6;
  border-radius: var(--border-radius);
  overflow: hidden;
}

.header-cell, .class-cell {
  background-color: #fff;
  padding: 8px;
  min-height: 80px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.header-cell {
  background-color: #f8f9fa;
  font-weight: 600;
  color: var(--color-text-primary);
}

.time-header .time-range {
  font-size: 0.8em;
  color: var(--color-text-secondary);
  font-weight: normal;
  margin-top: 4px;
}

.class-cell {
  position: relative;
  transition: background-color 0.2s;
}

.course-item {
  width: 100%;
  height: 100%;
  padding: 8px;
  box-sizing: border-box;
  border-radius: 4px;
  background-color: var(--color-primary-light);
  color: var(--color-primary);
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  font-size: 0.9em;
  line-height: 1.3;
}
.course-item strong {
  font-weight: bold;
  color: var(--color-text-primary);
}
.course-item span {
  font-size: 0.9em;
  color: var(--color-text-secondary);
}
</style>