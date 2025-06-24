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
      // 同时获取可选课程、我的选课、我的个人档案
      const [offeringsRes, enrollmentsRes, profileRes] = await Promise.all([
        enrollmentService.getAvailableOfferings(),
        studentService.getMyCoursesAndGrades(),
        studentService.getMyProfile()
      ]);
      availableOfferings.value = offeringsRes;
      myEnrollments.value = enrollmentsRes;
      studentProfile.value = profileRes; // 保存学生档案，其中包含 classId
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

/**
 * 【核心修正】更新判断课程类型的逻辑
 * 从基于 majorId 判断，改为基于 studentProfile.classId 判断
 */
const getCourseTypeForStudent = (offering) => {
  // 检查学生档案和课程的班级关联列表是否存在
  if (!studentProfile.value || !offering.associatedClasses) return 'ELECTIVE';

  // 获取当前学生的班级ID
  const studentClassId = studentProfile.value.classId;

  // 在课程的关联班级列表中，查找是否有与当前学生班级匹配的记录
  const association = offering.associatedClasses.find(c => c.classId === studentClassId);

  // 如果找到匹配记录，返回其课程类型；否则，默认为选修
  return association ? association.courseType : 'ELECTIVE';
};

// isCompulsory 会自动因 getCourseTypeForStudent 的修正而变正确
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
    await fetchData(); // 重新加载数据以更新界面
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
    await fetchData(); // 重新加载数据
  } catch (error) {
    // 后端返回的明确错误信息会被显示出来
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
</script>

<style scoped>
/* 样式与之前保持一致，此处省略 */
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
.closed-notice h2 { margin: 16px 0; }
.closed-notice p { color: var(--color-text-secondary); max-width: 400px; margin: 0 auto 24px auto; }
</style>