<template>
  <div class="page-container">
    <header class="page-header">
      <h1>我的课表与成绩</h1>
      <p>在此查看您的课程安排、已修学分和成绩详情。</p>
    </header>

    <div v-if="isLoading" class="loading-indicator">正在加载您的课程数据...</div>

    <div v-else>
      <div class="content-card timetable-card">
        <div class="timetable">
          <div class="header-cell time-col-header">时间</div>
          <div v-for="day in days" :key="day" class="header-cell day-header">{{ day }}</div>

          <template v-for="timeSlot in timeSlots" :key="timeSlot.id">
            <div class="header-cell time-header">
              <div class="time-slot-id">第 {{ timeSlot.id }} 节</div>
              <div class="time-range">{{ timeSlot.range }}</div>
            </div>
            <div v-for="dayIndex in 7" :key="`${timeSlot.id}-${dayIndex}`" class="class-cell">
              <div v-if="getCourseAt(dayIndex, timeSlot.id)" class="courseCatalog-item">
                <div class="courseCatalog-details">
                  <strong>{{ getCourseAt(dayIndex, timeSlot.id).courseName }}</strong>
                  <span>{{ getCourseAt(dayIndex, timeSlot.id).teacherName || 'N/A' }}</span>
                </div>
              </div>
              <div v-else class="empty-cell"></div>
            </div>
          </template>
        </div>
      </div>

      <div class="content-card" style="margin-top: 24px;">
        <div class="summary-card">
          <div class="summary-item">
            <span class="summary-label">已修总学分:</span>
            <span class="summary-value">{{ totalCredits }}</span>
          </div>
        </div>
        <table class="data-table my-cours-table">
          <thead>
          <tr>
            <th>课程编号</th>
            <th>课程名称</th>
            <th>学分</th>
            <th class="score-col">我的成绩</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="item in myEnrollments" :key="item.enrollmentId" class="table-row">
            <td>{{ item.courseId }}</td>
            <td>{{ item.courseName }}</td>
            <td>{{ item.credits }}</td>
            <td class="score-col">
                <span :class="getScoreClass(item.score)" class="score-display">
                  {{ item.score !== null ? item.score : '暂无' }}
                </span>
            </td>
          </tr>
          <tr v-if="myEnrollments.length === 0">
            <td colspan="4" class="no-data-cell">您尚未选修任何课程。</td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { studentService } from '@/services/apiService';
import { showNotification } from '@/services/notificationStore';

const isLoading = ref(true);
const myEnrollments = ref([]);

// 课表的时间和日期定义
const days = ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日'];
const timeSlots = [
  { id: 1, range: '8:00-9:40' },
  { id: 2, range: '10:00-11:40' },
  { id: 3, range: '14:00-15:40' },
  { id: 4, range: '16:00-17:40' },
  { id: 5, range: '19:00-20:40' }
];

const fetchMyScheduleAndGrades = async () => {
  isLoading.value = true;
  try {
    myEnrollments.value = await studentService.getMyCoursesAndGrades();
  } catch (error) {
    showNotification('加载课表与成绩失败', 'error');
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchMyScheduleAndGrades);

const courseMap = computed(() => {
  const map = new Map();
  myEnrollments.value.forEach(courseCatalog => {
    if (courseCatalog.courseDay && courseCatalog.courseTime) {
      const key = `${courseCatalog.courseDay}-${courseCatalog.courseTime}`;
      map.set(key, courseCatalog);
    }
  });
  return map;
});

const totalCredits = computed(() => {
  return myEnrollments.value.reduce((sum, item) => sum + (item.credits || 0), 0);
});

const getCourseAt = (day, time) => {
  const key = `${day}-${time}`;
  return courseMap.value.get(key);
};

const getScoreClass = (score) => {
  if (score === null) return 'score-pending';
  if (score >= 60) return 'score-pass';
  return 'score-fail';
}
</script>

<style scoped>
.timetable-card { padding: 0; overflow-x: auto; }
.timetable { display: grid; grid-template-columns: 120px repeat(7, minmax(150px, 1fr)); gap: 1px; background-color: var(--color-border); border-radius: var(--border-radius); overflow: hidden; min-width: 900px; }
.header-cell, .class-cell { background-color: var(--color-surface); padding: 10px; min-height: 80px; display: flex; flex-direction: column; justify-content: center; align-items: center; text-align: center; }
.header-cell { background-color: var(--color-background); font-weight: 600; }
.time-header { justify-content: space-around; }
.time-slot-id { font-size: 1.1em; font-weight: 700; color: var(--color-primary); }
.time-range { font-size: 0.8em; color: var(--color-text-secondary); }
.courseCatalog-item { width: 100%; height: 100%; padding: 8px; box-sizing: border-box; border-radius: 6px; background-color: var(--color-primary-light); color: var(--color-primary); }
.courseCatalog-details strong { color: var(--color-text-primary); }
.courseCatalog-details span { font-size: 0.85em; color: var(--color-text-secondary); }
.empty-cell { background-color: #fcfdff; width: 100%; height: 100%; border-radius: var(--border-radius); }

.summary-card { padding: 20px; background-color: #f8f9fa; border-bottom: 1px solid var(--color-border); }
.summary-item { display: flex; align-items: baseline; gap: 10px; }
.summary-label { font-size: 1em; color: var(--color-text-secondary); }
.summary-value { font-size: 1.5em; font-weight: bold; color: var(--color-primary); }

.my-cours-table th, .my-cours-table td { padding: 14px 18px; vertical-align: middle; }
.score-col { text-align: center; font-weight: bold; font-size: 1.1em; width: 150px; }
.score-display.score-pass { color: var(--color-success); }
.score-display.score-fail { color: var(--color-danger); }
.score-display.score-pending { color: var(--color-text-secondary); font-weight: normal; font-style: italic; }
.no-data-cell { text-align: center; padding: 40px; }
</style>