<template>
  <div class="page-container">
    <header class="page-header">
      <h1>我的课表</h1>
      <p>以下是您本学期已选课程的时间安排。</p>
    </header>

    <div v-if="isLoading" class="loading-indicator">正在生成您的课表...</div>

    <div v-else class="content-card timetable-card">
      <div class="timetable">
        <div class="header-cell time-col-header">时间</div>
        <div v-for="day in days" :key="day" class="header-cell day-header">{{ day }}</div>

        <template v-for="timeSlot in timeSlots" :key="timeSlot.id">
          <div class="header-cell time-header">
            <div class="time-slot-id">第 {{ timeSlot.id }} 节</div>
            <div class="time-range">{{ timeSlot.range }}</div>
          </div>
          <div v-for="dayIndex in 7" :key="`${timeSlot.id}-${dayIndex}`" class="class-cell">
            <div v-if="getCourseAt(dayIndex, timeSlot.id)" class="course-item">
              <div class="course-icon-wrapper">
                <svg class="course-icon" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20V6.5A2.5 2.5 0 0 0 17.5 4h-11A2.5 2.5 0 0 0 4 6.5v13z"></path>
                </svg>
              </div>
              <div class="course-details">
                <strong>{{ getCourseAt(dayIndex, timeSlot.id).courseName }}</strong>
                <span>{{ getCourseAt(dayIndex, timeSlot.id).teacherName || 'N/A' }}</span>
                <span>学分: {{ getCourseAt(dayIndex, timeSlot.id).credits }}</span>
              </div>
            </div>
            <div v-else class="empty-cell"></div>
          </div>
        </template>
      </div>
      <div v-if="myCourses.length === 0" class="no-courses-placeholder">
        <svg class="no-data-icon" xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10"></circle>
          <line x1="8" y1="12" x2="16" y2="12"></line>
        </svg>
        <p>您本学期暂未选修任何课程。</p>
        <router-link to="/student/available-courses" class="enroll-now-btn">前往选课中心</router-link>
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
    // 确保 courseDay 和 courseTime 存在，并用于构建唯一的键
    if (course.courseDay && course.courseTime) {
      const key = `${course.courseDay}-${course.courseTime}`;
      // 如果同一个时间段有多门课程，这里只会存储最后一门。
      // 对于真实的课表，应该有更复杂的冲突处理或多课程显示逻辑。
      // 目前按单门课占据一个时段处理。
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

/* 课表卡片和通用样式 */
.timetable-card {
  padding: 0; /* 移除内边距，让表格铺满 */
  overflow-x: auto; /* 允许水平滚动 */
}

.timetable {
  display: grid;
  grid-template-columns: 120px repeat(7, minmax(150px, 1fr)); /* 1列时间头 + 7列日期，每列最小宽度150px */
  gap: 1px; /* 细小的单元格边框 */
  background-color: var(--color-border); /* 模拟表格边框色 */
  border-radius: var(--border-radius);
  overflow: hidden; /* 确保内容在圆角内 */
  min-width: 900px; /* 确保表格在小屏幕下能滚动，而不是压缩 */
}

/* 单元格基础样式 */
.header-cell, .class-cell {
  background-color: var(--color-surface); /* 白色背景 */
  padding: 10px;
  min-height: 90px; /* 确保单元格有足够高度 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  box-sizing: border-box; /* 边框和内边距包含在宽度内 */
}

/* 头部单元格样式 */
.header-cell {
  background-color: var(--color-background); /* 标题行和列使用更浅的背景色 */
  font-weight: 600;
  color: var(--color-text-primary);
  font-size: 0.95em;
  padding: 15px 10px; /* 增加上下内边距 */
}

.time-col-header {
  border-right: 1px solid var(--color-border); /* 时间列右侧边框 */
}

.day-header {
  border-bottom: 1px solid var(--color-border); /* 日期头部底部边框 */
}

.time-header {
  justify-content: space-around; /* 时间段上下居中分布 */
  border-right: 1px solid var(--color-border); /* 时间段右侧边框 */
}

.time-slot-id {
  font-size: 1.1em;
  font-weight: 700;
  color: var(--color-primary);
}

.time-range {
  font-size: 0.8em;
  color: var(--color-text-secondary);
  font-weight: normal;
  margin-top: 4px;
}

/* 课程单元格样式 */
.class-cell {
  position: relative;
  transition: background-color 0.2s;
  cursor: default; /* 默认鼠标样式 */
}

.class-cell:hover {
  background-color: var(--color-primary-light); /* 课程单元格hover效果 */
}

.course-item {
  width: 100%;
  height: 100%;
  padding: 8px;
  box-sizing: border-box;
  border-radius: 6px; /* 课程项自身圆角 */
  background-color: var(--color-primary-light); /* 课程项背景色 */
  color: var(--color-primary);
  display: flex;
  flex-direction: row; /* 图标和文字并排 */
  align-items: flex-start; /* 顶部对齐 */
  gap: 8px; /* 图标和文字间距 */
  font-size: 0.9em;
  line-height: 1.3;
  text-align: left; /* 文字左对齐 */
  box-shadow: 0 2px 4px rgba(0,0,0,0.05); /* 轻微阴影 */
  transition: all 0.2s ease;
}

.course-item:hover {
  transform: translateY(-2px); /* 悬浮上移 */
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.course-icon-wrapper {
  flex-shrink: 0; /* 防止图标被压缩 */
  padding-top: 2px; /* 微调图标位置 */
}

.course-icon {
  color: var(--color-primary); /* 图标颜色 */
}

.course-details {
  display: flex;
  flex-direction: column;
}

.course-item strong {
  font-weight: bold;
  color: var(--color-text-primary);
  font-size: 1.05em; /* 课程名稍大 */
}
.course-item span {
  font-size: 0.85em;
  color: var(--color-text-secondary);
}

.empty-cell {
  background-color: #fcfdff; /* 空白单元格背景色稍有不同 */
  width: 100%;
  height: 100%;
  border-radius: var(--border-radius);
}

/* 空课程提示 */
.no-courses-placeholder {
  text-align: center;
  padding: 60px 20px;
  color: var(--color-text-secondary);
  background-color: var(--color-surface);
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}
.no-data-icon {
  color: var(--color-border);
  opacity: 0.8;
  width: 64px;
  height: 64px;
}
.no-courses-placeholder p {
  margin: 0;
  font-size: 1.2em;
  font-weight: 500;
}
.enroll-now-btn {
  background-color: var(--color-primary);
  color: white;
  border: none;
  padding: 12px 25px;
  font-size: 1em;
  font-weight: 600;
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: all 0.2s ease;
  text-decoration: none; /* 移除链接下划线 */
}
.enroll-now-btn:hover {
  opacity: 0.9;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0,0,0,0.15);
}
</style>