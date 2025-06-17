<template>
  <div class="page-container">
    <header class="page-header">
      <h1>选课中心</h1>
      <p v-if="isSelectionOpen">在这里浏览、选择或退选课程。</p>
      <p v-else>当前非选课/退课时间。</p>
    </header>

    <div v-if="isLoading" class="loading-indicator">正在加载...</div>
    <div v-else-if="!isSelectionOpen" class="content-card closed-notice">
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
          <tr v-for="offering in availableOfferings" :key="offering.id" class="table-row">
            <td>{{ offering.courseCode }}</td>
            <td>{{ offering.courseName }}</td>
            <td>
                <span class="course-type" :class="getCourseTypeForStudent(offering).toLowerCase()">
                  {{ formatCourseType(getCourseTypeForStudent(offering)) }}
                </span>
            </td>
            <td>{{ offering.credits }}</td>
            <td>{{ offering.teacherName }}</td>
            <td>{{ formatCourseTime(offering.courseDay, offering.courseTime) }}</td>
            <td class="action-col">
              <template v-if="isEnrolled(offering.id)">
                  <span v-if="isCompulsory(offering) || hasGrade(offering.id)" class="status-tag non-withdrawable-tag">
                    不可退
                  </span>
                <button v-else @click="handleWithdraw(getEnrollmentId(offering.id))" class="action-btn withdraw-btn" :disabled="isWithdrawing">
                  退课
                </button>
              </template>
              <template v-else>
                <button @click="handleEnroll(offering.id)" class="action-btn enroll-btn" :disabled="isEnrolling || hasConflict(offering) || isCompulsory(offering)">
                  <span v-if="isCompulsory(offering)">系统预置</span>
                  <span v-else-if="hasConflict(offering)">时间冲突</span>
                  <span v-else>选课</span>
                </button>
              </template>
            </td>
          </tr>
          <tr v-if="availableOfferings.length === 0">
            <td colspan="7" class="no-data-cell">当前学期未找到符合条件的课程。</td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useAuthStore } from '@/stores/auth'; // 用于获取学生专业信息
import { enrollmentService, studentService } from '@/services/apiService';
import { apiService as systemSettingApiService } from '@/services/systemSettingService';
import { showNotification } from '@/services/notificationStore';

const authStore = useAuthStore();
const availableOfferings = ref([]);
const myEnrollments = ref([]);
const isLoading = ref(true);
const isEnrolling = ref(false);
const isWithdrawing = ref(false);
const isSelectionOpen = ref(false);
const studentProfile = ref(null);

const fetchData = async () => {
  isLoading.value = true;
  try {
    const statusRes = await systemSettingApiService.getCourseSelectionStatus();
    isSelectionOpen.value = statusRes.isOpen;

    if (isSelectionOpen.value) {
      const [offeringsRes, enrollmentsRes, profileRes] = await Promise.all([
        enrollmentService.getAvailableOfferings(),
        studentService.getMyCoursesAndGrades(),
        studentService.getMyProfile() // 获取学生个人信息，特别是majorId
      ]);
      availableOfferings.value = offeringsRes;
      myEnrollments.value = enrollmentsRes;
      studentProfile.value = profileRes;
    }
  } catch (error) {
    showNotification(error.message || '数据加载失败', 'error');
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchData);

const myCourseMap = ref(new Map());
watch(availableCourses, (newCourses) => {
  const map = new Map();
  newCourses.forEach(c => map.set(c.id, c));
  myCourseMap.value = map;
}, { deep: true });

const mySchedule = computed(() => new Set(myEnrollments.value.filter(e => e.courseDay && e.courseTime).map(e => `${e.courseDay}-${e.courseTime}`)));

const isEnrolled = (courseDbId) => {
  const courseCatalog = myCourseMap.value.get(courseDbId);
  if (!courseCatalog) return false;
  return myEnrollments.value.some(e => e.courseId === courseCatalog.courseId);
};

const getEnrollmentId = (courseDbId) => {
  const courseCatalog = myCourseMap.value.get(courseDbId);
  if (!courseCatalog) return null;
  return myEnrollments.value.find(e => e.courseId === courseCatalog.courseId)?.enrollmentId;
};

const hasGrade = (courseDbId) => {
  const courseCatalog = myCourseMap.value.get(courseDbId);
  if (!courseCatalog) return false;
  const enrollment = myEnrollments.value.find(e => e.courseId === courseCatalog.courseId);
  return enrollment && enrollment.score !== null;
};

const isCompulsory = (courseDbId) => {
  const courseCatalog = myCourseMap.value.get(courseDbId);
  return courseCatalog && courseCatalog.courseType === 'COMPULSORY';
};

const hasConflict = (courseCatalog) => {
  if (isEnrolled(courseCatalog.id)) return false;
  if (!courseCatalog.courseDay || !courseCatalog.courseTime) return false;
  return mySchedule.value.has(`${courseCatalog.courseDay}-${courseCatalog.courseTime}`);
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

.courseCatalog-type {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.85em;
  font-weight: 600;
  color: white;
}
.courseCatalog-type.compulsory {
  background-color: var(--color-danger);
}
.courseCatalog-type.elective {
  background-color: var(--color-success);
}
.status-tag {
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 0.9em;
  background-color: #e9ecef;
  color: var(--color-text-secondary);
}
.non-withdrawable-tag {
  background-color: #f8d7da;
  color: #721c24;
}
.action-btn {
  margin-right: 8px;
  padding: 6px 12px;
  border-radius: var(--border-radius);
  border: 1px solid transparent;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
  font-size: 0.9em;
}
.action-btn:disabled {
  background-color: #e9ecef !important;
  color: #adb5bd !important;
  cursor: not-allowed;
  border-color: transparent !important;
}
.enroll-btn {
  background-color: var(--color-success);
  color: white;
}
.withdraw-btn {
  background-color: transparent;
  color: var(--color-danger);
  border: 1px solid var(--color-danger);
}
.withdraw-btn:hover {
  background-color: var(--color-danger);
  color: white;
}
.closed-notice {
  text-align: center;
  padding: 40px;
}
.closed-notice .notice-icon {
  font-size: 48px;
  color: var(--color-warning);
}
.closed-notice h2 {
  margin: 16px 0;
}
.closed-notice p {
  color: var(--color-text-secondary);
  max-width: 400px;
  margin: 0 auto 24px auto;
}
.closed-notice .action-btn {
  background-color: var(--color-primary);
  color: white;
  text-decoration: none;
}
</style>