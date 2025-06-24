<template>
  <div class="page-container">
    <header class="page-header">
      <h1>选课中心</h1>
      <p v-if="isSelectionOpen">在这里浏览、选择或退选课程。</p>
      <p v-else>当前非选课/退课时间。</p>
    </header>

    <div v-if="isLoading" class="loading-indicator">正在加载...</div>
    <div v-else-if="!isSelectionOpen" class="content-card closed-notice">
      <h2>选课通道已关闭</h2>
      <p>请关注学校通知，在指定时间内进行选课操作。</p>
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
            <th>上课教室</th>
            <th>容量/已选</th>
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
            <td>{{ offering.teacherName || '待定' }}</td>
            <td>{{ formatCourseTime(offering.courseDay, offering.courseTime) }}</td>
            <td>{{ offering.classroomName || '待定' }}</td>
            <td>
              <span :class="getCapacityClass(offering)">
                {{ offering.capacity != null ? offering.capacity : '不限' }} / {{ offering.currentEnrollment }}
              </span>
            </td>
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
                <button @click="handleEnroll(offering.id)"
                        class="action-btn enroll-btn"
                        :disabled="isEnrolling || hasConflict(offering) || isCompulsory(offering) || isFull(offering)">
                  <span v-if="isFull(offering)">已满</span>
                  <span v-else-if="isCompulsory(offering)">系统预置</span>
                  <span v-else-if="hasConflict(offering)">时间冲突</span>
                  <span v-else>选课</span>
                </button>
              </template>
            </td>
          </tr>
          <tr v-if="availableOfferings.length === 0">
            <td colspan="9" class="no-data-cell">当前学期未找到符合条件的课程。</td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { enrollmentService, studentService } from '@/services/apiService';
import { apiService as systemSettingApiService } from '@/services/systemSettingService';
import { showNotification } from '@/services/notificationStore';

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
        studentService.getMyProfile()
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

const mySchedule = computed(() => {
  const scheduleSet = new Set();
  myEnrollments.value.forEach(enrollment => {
    if (enrollment.courseDay && enrollment.courseTime) {
      scheduleSet.add(`${enrollment.courseDay}-${enrollment.courseTime}`);
    }
  });
  return scheduleSet;
});

const enrolledOfferingIds = computed(() => new Set(myEnrollments.value.map(e => e.courseOfferingId)));

const enrollmentMap = computed(() => {
  const map = new Map();
  myEnrollments.value.forEach(e => map.set(e.courseOfferingId, e));
  return map;
});


const isEnrolled = (offeringId) => enrolledOfferingIds.value.has(offeringId);

const getEnrollmentId = (offeringId) => enrollmentMap.value.get(offeringId)?.enrollmentId;

const hasGrade = (offeringId) => {
  const enrollment = enrollmentMap.value.get(offeringId);
  return enrollment && enrollment.score !== null;
};

const getCourseTypeForStudent = (offering) => {
  if (!studentProfile.value || !offering.associatedClasses) return 'ELECTIVE';
  const studentClassId = studentProfile.value.classId;
  const association = offering.associatedClasses.find(c => c.classId === studentClassId);
  return association ? association.courseType : 'ELECTIVE';
};

const isCompulsory = (offering) => getCourseTypeForStudent(offering) === 'COMPULSORY';

const hasConflict = (offering) => {
  if (!offering.courseDay || !offering.courseTime) return false;
  if(isEnrolled(offering.id)) return false;
  return mySchedule.value.has(`${offering.courseDay}-${offering.courseTime}`);
};

const handleEnroll = async (offeringId) => {
  isEnrolling.value = true;
  try {
    await enrollmentService.enrollInCourse(offeringId);
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

const formatCourseType = (type) => {
  if (type === 'COMPULSORY') return '专业必修';
  return '专业选修';
}

const isFull = (offering) => {
  return offering.capacity != null && offering.currentEnrollment >= offering.capacity;
};

const getCapacityClass = (offering) => {
  if (offering.capacity == null) return 'capacity-unlimited';
  const ratio = offering.currentEnrollment / offering.capacity;
  if (ratio >= 1) return 'capacity-full';
  if (ratio >= 0.8) return 'capacity-warning';
  return 'capacity-normal';
};
</script>

<style scoped>
.course-type { padding: 4px 8px; border-radius: 4px; font-size: 0.85em; font-weight: 600; color: white; }
.course-type.compulsory { background-color: var(--color-danger); }
.course-type.elective { background-color: var(--color-success); }
.status-tag { font-weight: 600; padding: 6px 12px; border-radius: 16px; font-size: 0.9em; background-color: #e9ecef; color: var(--color-text-secondary); }
.non-withdrawable-tag { background-color: #f8d7da; color: #721c24; }
.action-btn { margin-right: 8px; padding: 6px 12px; border-radius: var(--border-radius); border: 1px solid transparent; cursor: pointer; font-weight: 500; transition: all 0.2s; font-size: 0.9em; }
.action-btn:disabled { background-color: #e9ecef !important; color: #adb5bd !important; cursor: not-allowed; border-color: transparent !important; }
.enroll-btn { background-color: var(--color-success); color: white; }
.withdraw-btn { background-color: transparent; color: var(--color-danger); border: 1px solid var(--color-danger); }
.withdraw-btn:hover { background-color: var(--color-danger); color: white; }
.closed-notice { text-align: center; padding: 40px; }

.capacity-normal { color: var(--color-success); }
.capacity-warning { color: #e67e22; font-weight: bold; }
.capacity-full { color: var(--color-danger); font-weight: bold; }
.capacity-unlimited { color: var(--color-text-secondary); font-style: italic; }
</style>