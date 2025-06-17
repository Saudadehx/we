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
      <div class="search-panel content-card">
        <div class="search-item">
          <label for="searchCourseName">课程名称</label>
          <input id="searchCourseName" v-model="searchParams.courseName" @keyup.enter="fetchData" placeholder="模糊搜索课程名">
        </div>
        <div class="search-item">
          <label for="searchCourseId">课程编号</label>
          <input id="searchCourseId" v-model="searchParams.courseId" @keyup.enter="fetchData" placeholder="模糊搜索课程编号">
        </div>
        <div class="search-item">
          <label for="searchTeacherName">教师姓名</label>
          <input id="searchTeacherName" v-model="searchParams.teacherName" @keyup.enter="fetchData" placeholder="模糊搜索教师名">
        </div>
        <div class="search-actions">
          <button @click="fetchData" class="search-btn">搜索</button>
          <button @click="resetSearch" class="reset-btn">重置</button>
        </div>
      </div>

      <div class="content-card" style="margin-top: 24px;">
        <table class="data-table available-courses-table">
          <thead>
          <tr>
            <th>课程编号</th>
            <th>课程名称</th>
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
            <td>{{ course.credits }}</td>
            <td>{{ course.teacherName }}</td>
            <td>{{ formatCourseTime(course.courseDay, course.courseTime) }}</td>
            <td class="action-col">
              <template v-if="isEnrolled(course.courseId)">
                <button
                    v-if="canWithdraw(course.courseId)"
                    @click="handleWithdraw(getEnrollmentId(course.courseId))"
                    class="action-btn withdraw-btn"
                    :disabled="isWithdrawing"
                >
                  退课
                </button>
                <span v-else class="status-tag enrolled-tag">
                    {{ hasGrade(course.courseId) ? '已有成绩' : '已选' }}
                  </span>
              </template>
              <template v-else>
                <button
                    @click="handleEnroll(course.id)"
                    class="action-btn enroll-btn"
                    :disabled="isEnrolling || hasConflict(course)"
                >
                  <span v-if="hasConflict(course)">时间冲突</span>
                  <span v-else>选课</span>
                </button>
              </template>
            </td>
          </tr>
          <tr v-if="availableCourses.length === 0">
            <td colspan="6" class="no-data-cell">未找到符合条件的课程。</td>
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

const searchParams = ref({
  courseName: '',
  courseId: '',
  teacherName: ''
});

const fetchData = async () => {
  isLoading.value = true;
  try {
    const [statusRes, coursesRes, enrollmentsRes] = await Promise.all([
      systemSettingApiService.getCourseSelectionStatus(),
      courseService.getAll(searchParams.value),
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

const resetSearch = () => {
  searchParams.value = { courseName: '', courseId: '', teacherName: '' };
  fetchData();
};

const myEnrollmentMap = computed(() => new Map(myEnrollments.value.map(e => [e.courseId, e])));
const mySchedule = computed(() => new Set(myEnrollments.value.filter(e => e.courseDay && e.courseTime).map(e => `${e.courseDay}-${e.courseTime}`)));

const isEnrolled = (courseId) => myEnrollmentMap.value.has(courseId);
const getEnrollmentId = (courseId) => myEnrollmentMap.value.get(courseId)?.enrollmentId;
const hasGrade = (courseId) => {
  const enrollment = myEnrollmentMap.value.get(courseId);
  return enrollment && enrollment.score !== null;
};
const canWithdraw = (courseId) => isEnrolled(courseId) && !hasGrade(courseId);

const hasConflict = (course) => {
  if (isEnrolled(course.courseId)) return false;
  if (!course.courseDay || !course.courseTime) return false;
  return mySchedule.value.has(`${course.courseDay}-${course.courseTime}`);
};

const handleEnroll = async (courseId) => {
  isEnrolling.value = true;
  try {
    await enrollmentService.enrollInCourse(courseId);
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
  if (!day || !time) return '未安排';
  const dayStr = '星期' + '一二三四五六日'[day-1];
  const timeStr = `第 ${time} 大节`;
  return `${dayStr} ${timeStr}`;
};
</script>

<style scoped>
@import '@/assets/styles/common-page.css';
/* ... (可以从旧的 AvailableCourses.vue 复制大部分样式) ... */
.search-panel { display: flex; flex-wrap: wrap; gap: 16px; align-items: flex-end; padding: 16px 24px; margin-bottom: 24px; }
.search-item { display: flex; flex-direction: column; min-width: 180px; flex-grow: 1; }
.search-item label { font-size: 0.9em; color: var(--color-text-secondary); margin-bottom: 4px; }
.search-item input { padding: 8px 12px; border: 1px solid var(--color-border); border-radius: var(--border-radius); font-size: 1em; }
.search-actions { display: flex; gap: 8px; }
.search-btn, .reset-btn { padding: 8px 20px; border-radius: var(--border-radius); border: none; cursor: pointer; font-weight: 500; }
.search-btn { background-color: var(--color-primary); color: white; }
.reset-btn { background-color: #f1f3f5; color: var(--color-text-secondary); }

.action-btn { min-width: 90px; text-align: center; }
.enroll-btn { background-color: var(--color-primary); color: white; }
.enroll-btn:disabled { background-color: #e0e0e0; color: #a0a0a0; cursor: not-allowed; }
.withdraw-btn { background-color: var(--color-danger); color: white; }
.status-tag { display: inline-block; padding: 6px 12px; border-radius: 16px; font-size: 0.9em; font-weight: 500; }
.enrolled-tag { background-color: var(--color-primary-light); color: var(--color-primary); }

.closed-notice { text-align: center; padding: 40px; }
.notice-icon { font-size: 3em; margin-bottom: 16px; }
.closed-notice h2 { margin-bottom: 16px; }
.closed-notice p { color: var(--color-text-secondary); margin-bottom: 24px; }
.closed-notice .action-btn { background: var(--color-primary); color: #fff; text-decoration: none; padding: 10px 20px; }
</style>