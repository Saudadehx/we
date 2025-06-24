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
              <div v-if="getCourseAt(dayIndex, timeSlot.id)" class="course-item">
                <div class="course-details">
                  <strong>{{ getCourseAt(dayIndex, timeSlot.id).courseName }}</strong>
                  <span>{{ getCourseAt(dayIndex, timeSlot.id).teacherName || 'N/A' }}</span>
                  <span class="course-location">{{ getCourseAt(dayIndex, timeSlot.id).classroomName || '教室待定' }}</span>
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
            <span class="summary-value">{{ totalCredits.toFixed(1) }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">平均绩点 (GPA):</span>
            <span class="summary-value">{{ gpa.toFixed(2) }}</span>
          </div>
        </div>
        <table class="data-table my-courses-table">
          <thead>
          <tr>
            <th>课程名称</th>
            <th>课程编号</th>
            <th>学分</th>
            <th>授课教师</th>
            <th class="score-col">成绩</th>
          </tr>
          </thead>
          <tbody>
          <tr v-if="myEnrollments.length === 0">
            <td colspan="5" class="no-data-cell">您当前学期没有已选课程。</td>
          </tr>
          <tr v-for="item in myEnrollments" :key="item.enrollmentId">
            <td>{{ item.courseName }}</td>
            <td>{{ item.courseId }}</td>
            <td>{{ item.credits }}</td>
            <td>{{ item.teacherName }}</td>
            <td class="score-col">
                        <span class="score-display" :class="getScoreClass(item.score)">
                            {{ item.score === null ? '暂无成绩' : item.score.toFixed(1) }}
                        </span>
            </td>
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
  myEnrollments.value.forEach(course => {
    if (course.courseDay && course.courseTime) {
      const key = `${course.courseDay}-${course.courseTime}`;
      map.set(key, course);
    }
  });
  return map;
});

const totalCredits = computed(() => {
  return myEnrollments.value.reduce((sum, item) => {
    // 只有及格的课程才计入已修学分
    if (item.score !== null && item.score >= 60) {
      return sum + (item.credits || 0);
    }
    return sum;
  }, 0);
});

const gpa = computed(() => {
  let totalCreditPoints = 0;
  let totalCreditsForGpa = 0;

  myEnrollments.value.forEach(item => {
    if (item.score !== null) {
      const credits = item.credits || 0;
      const score = item.score;
      let point = 0;
      if (score >= 60) {
        point = (score - 50) / 10;
      }
      totalCreditPoints += point * credits;
      totalCreditsForGpa += credits;
    }
  });

  if (totalCreditsForGpa === 0) {
    return 0.00;
  }
  return totalCreditPoints / totalCreditsForGpa;
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
/* 课表通用样式 */
.course-item {
  width: 100%; height: 100%; padding: 8px; box-sizing: border-box;
  border-radius: 6px; background-color: var(--color-primary-light);
  display: flex; flex-direction: column; justify-content: center;
  text-align: center;
}
.course-details {
  display: flex; flex-direction: column; gap: 4px;
}
.course-details strong { color: var(--color-text-primary); font-weight: 600; font-size: 0.95em; }
.course-details span { font-size: 0.85em; color: var(--color-text-secondary); }
.course-details .course-location {
  font-style: italic;
  font-size: 0.8em;
}
.timetable-card { padding: 0; overflow-x: auto; }
.timetable { display: grid; grid-template-columns: 100px repeat(7, minmax(140px, 1fr)); gap: 1px; background-color: var(--color-border); border-radius: var(--border-radius); overflow: hidden; min-width: 900px; }
.header-cell, .class-cell { background-color: var(--color-surface); padding: 10px; min-height: 80px; display: flex; flex-direction: column; justify-content: center; align-items: center; }
.header-cell { background-color: #f8f9fa; font-weight: 600; }
.time-header { justify-content: space-around; }
.time-slot-id { font-size: 1.1em; font-weight: 700; color: var(--color-primary); }
.time-range { font-size: 0.8em; color: var(--color-text-secondary); }
.empty-cell { background-color: #fcfdff; width: 100%; height: 100%; border-radius: var(--border-radius); }

/* 成绩与统计卡片样式 */
.summary-card {
  padding: 20px;
  background-color: #f8f9fa;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  gap: 48px;
  border-top-left-radius: var(--border-radius);
  border-top-right-radius: var(--border-radius);
}
.summary-item {
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.summary-label {
  font-size: 1em;
  color: var(--color-text-secondary);
}
.summary-value {
  font-size: 1.5em;
  font-weight: bold;
  color: var(--color-primary);
}

.my-courses-table th, .my-courses-table td {
  padding: 14px 18px;
  vertical-align: middle;
}
.score-col {
  text-align: center;
  font-weight: bold;
  font-size: 1.1em;
  width: 150px;
}
.score-display.score-pass { color: var(--color-success); }
.score-display.score-fail { color: var(--color-danger); }
.score-display.score-pending { color: var(--color-text-secondary); font-weight: normal; font-style: italic; }
.no-data-cell {
  text-align: center;
  padding: 40px;
  color: var(--color-text-secondary);
}
</style>