<template>
  <div class="page-container">
    <header class="page-header">
      <h1>选课中心</h1>
      <p v-if="isSelectionOpen">在这里浏览、选择或退选课程。</p>
      <p v-else>当前非选课/退课时间。</p>
    </header>

    <div v-if="isLoading" class="loading-indicator">正在加载...</div>

    <div v-else-if="!isSelectionOpen" class="content-card closed-notice">
      <div class="notice-icon">&#128711;</div>
      <h2>选课通道已关闭</h2>
      <p>当前不是选课或退课时间，请您关注教务通知，在规定时间内进行操作。</p>
      <router-link to="/student/schedule" class="action-btn">查看我的课表</router-link>
    </div>

    <div v-else>
      <div class="content-card">
        <table class="data-table available-courses-table">
          <thead>
          <tr>
            <th>课程编号</th>
            <th>课程名称</th>
            <th>课程类型</th>
            <th>学分</th>
            <th>授课教师</th>
            <th>上课时间</th>
            <th class="action-col">操作</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="course in availableCourses" :key="course.id" class="table-row">
            <td>{{ course.courseId }}</td>
            <td>{{ course.courseName }}</td>
            <td>
                <span class="course-type" :class="course.courseType.toLowerCase()">
                    {{ course.courseType === 'COMPULSORY' ? '必修' : '选修' }}
                </span>
            </td>
            <td>{{ course.credits }}</td>
            <td>{{ course.teacherName }}</td>
            <td>{{ formatCourseTime(course.courseDay, course.courseTime) }}</td>
            <td class="action-col">
              <template v-if="isEnrolled(course.id)">
                <button
                    v-if="canWithdraw(course.id)"
                    @click="handleWithdraw(getEnrollmentId(course.id))"
                    class="action-btn withdraw-btn"
                    :disabled="isWithdrawing"
                >
                  退课
                </button>
                <span v-else class="status-tag enrolled-tag">
                    {{ hasGrade(course.id) ? '已有成绩' : (isCompulsory(course.id) ? '必修' : '已选') }}
                </span>
              </template>
              <template v-else>
                <button
                    @click="handleEnroll(course.id)"
                    class="action-btn enroll-btn"
                    :disabled="isEnrolling || hasConflict(course) || isCompulsory(course.id)"
                >
                  <span v-if="isCompulsory(course.id)">系统预置</span>
                  <span v-else-if="hasConflict(course)">时间冲突</span>
                  <span v-else>选课</span>
                </button>
              </template>
            </td>
          </tr>
          <tr v-if="availableCourses.length === 0">
            <td colspan="7" class="no-data-cell">未找到符合条件的课程。</td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { courseService, enrollmentService, studentService } from '@/services/apiService';
import { apiService as systemSettingApiService } from '@/services/systemSettingService';
import { showNotification } from '@/services/notificationStore';

const availableCourses = ref([]);
const myEnrollments = ref([]);
const isLoading = ref(true);
const isEnrolling = ref(false);
const isWithdrawing = ref(false);
const isSelectionOpen = ref(false);

const fetchData = async () => {
  isLoading.value = true;
  try {
    const [statusRes, coursesRes, enrollmentsRes] = await Promise.all([
      systemSettingApiService.getCourseSelectionStatus(),
      // 【修改】调用新的专属接口
      enrollmentService.getAvailableCourses(),
      studentService.getMyCoursesAndGrades()
    ]);
    isSelectionOpen.value = statusRes.isOpen;
    availableCourses.value = coursesRes;
    myEnrollments.value = enrollmentsRes;
  } catch (error) {
    showNotification(error.message || '数据加载失败', 'error');
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchData);

const myEnrollmentMap = computed(() => new Map(myEnrollments.value.map(e => [e.courseId, e])));
const myCourseMap = computed(() => {
  const map = new Map();
  availableCourses.value.forEach(c => map.set(c.id, c));
  return map;
});

const mySchedule = computed(() => new Set(myEnrollments.value.filter(e => e.courseDay && e.courseTime).map(e => `${e.courseDay}-${e.courseTime}`)));

const isEnrolled = (courseDbId) => myEnrollments.value.some(e => e.courseId === myCourseMap.value.get(courseDbId)?.courseId);

const getEnrollmentId = (courseDbId) => {
  const course = myCourseMap.value.get(courseDbId);
  if (!course) return null;
  return myEnrollments.value.find(e => e.courseId === course.courseId)?.enrollmentId;
};

const hasGrade = (courseDbId) => {
  const course = myCourseMap.value.get(courseDbId);
  if (!course) return false;
  const enrollment = myEnrollments.value.find(e => e.courseId === course.courseId);
  return enrollment && enrollment.score !== null;
};

const isCompulsory = (courseDbId) => {
  const course = myCourseMap.value.get(courseDbId);
  return course && course.courseType === 'COMPULSORY';
};

const canWithdraw = (courseDbId) => {
  return isEnrolled(courseDbId) && !hasGrade(courseDbId) && !isCompulsory(courseDbId);
};

const hasConflict = (course) => {
  if (isEnrolled(course.id)) return false;
  if (!course.courseDay || !course.courseTime) return false;
  return mySchedule.value.has(`${course.courseDay}-${course.courseTime}`);
};

const handleEnroll = async (courseDbId) => {
  isEnrolling.value = true;
  try {
    await enrollmentService.enrollInCourse(courseDbId);
    showNotification('选课成功！', 'success');
    await fetchData();
  } catch (error) {
    showNotification(error.message || '选课失败', 'error');
  } finally {
    isEnrolling.value = false;
  }
};

const handleWithdraw = async (enrollmentId) => {
  if (!confirm('确定要退选这门课程吗？')) return;
  isWithdrawing.value = true;
  try {
    await enrollmentService.dropCourse(enrollmentId);
    showNotification('退课成功！', 'success');
    await fetchData();
  } catch (error) {
    showNotification(error.message || '退课失败', 'error');
  } finally {
    isWithdrawing.value = false;
  }
};

const formatCourseTime = (day, time) => {
  if (!day || !time) return '时间待定';
  const dayStr = '星期' + '一二三四五六日'[day-1];
  const timeStr = `第 ${time} 大节`;
  return `${dayStr} ${timeStr}`;
};
</script>

<style scoped>
@import '@/assets/styles/common-page.css';

.course-type {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.85em;
  font-weight: 600;
  color: white;
}
.course-type.compulsory {
  background-color: var(--color-danger);
}
.course-type.elective {
  background-color: var(--color-success);
}
</style>