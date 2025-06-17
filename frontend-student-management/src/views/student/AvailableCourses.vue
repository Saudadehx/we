<template>
  <div class="page-container">
    <header class="page-header">
      <h1>选课中心</h1>
      <p>在这里浏览所有开放的课程，并选择您感兴趣的进行学习。</p>
    </header>

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

    <div v-if="isLoading" class="loading-indicator">正在加载课程列表...</div>

    <div v-else class="content-card" style="margin-top: 24px;">
      <table class="data-table">
        <thead>
        <tr>
          <th>课程编号</th>
          <th>课程名称</th>
          <th>学分</th>
          <th>授课教师</th>
          <th>上课时间</th>
          <th style="text-align: center;">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="course in availableCourses" :key="course.id">
          <td>{{ course.courseId }}</td>
          <td>{{ course.courseName }}</td>
          <td>{{ course.credits }}</td>
          <td>{{ course.teacherName }}</td>
          <td>{{ formatCourseTime(course.courseDay, course.courseTime) }}</td>
          <td style="text-align: center;">
            <button @click="handleEnroll(course.id)" class="enroll-btn" :disabled="isButtonDisabled(course)">
              <span v-if="enrollingId === course.id">处理中...</span>
              <span v-else-if="isEnrolled(course)">已选</span>
              <span v-else-if="hasConflict(course)">时间冲突</span>
              <span v-else>选课</span>
            </button>
          </td>
        </tr>
        <tr v-if="availableCourses.length === 0">
          <td colspan="6" style="text-align: center; padding: 20px;">
            暂无课程或未找到符合条件的课程。
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { courseService, enrollmentService, studentService } from '@/services/apiService';
import { showNotification } from '@/services/notificationStore';

const availableCourses = ref([]);
const myEnrollments = ref([]);
const isLoading = ref(true);
const enrollingId = ref(null);

const searchParams = ref({
  courseName: '',
  courseId: '',
  teacherName: ''
});

const fetchData = async () => {
  isLoading.value = true;
  try {
    const [coursesRes, enrollmentsRes] = await Promise.all([
      courseService.getAll(searchParams.value),
      studentService.getMyCoursesAndGrades()
    ]);
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

/**
 * 计算属性：获取当前学生已选课程的 courseId 集合
 * 【修正】使用 courseId 来判断，因为这是课程的唯一标识符
 */
const enrolledCourseIds = computed(() => {
  return new Set(myEnrollments.value.map(e => e.courseId));
});

/**
 * 计算属性：获取当前学生课表的时间段集合
 */
const mySchedule = computed(() => {
  const scheduleSet = new Set();
  myEnrollments.value.forEach(e => {
    if (e.courseDay && e.courseTime) {
      scheduleSet.add(`${e.courseDay}-${e.courseTime}`);
    }
  });
  return scheduleSet;
});

const formatCourseTime = (day, time) => {
  if (!day || !time) return '未安排';
  const dayStr = '星期' + '一二三四五六日'[day-1];
  const timeStr = `第 ${time} 大节`;
  return `${dayStr} ${timeStr}`;
};

/**
 * 判断课程是否已选
 * @param {object} course - 完整的课程对象
 */
const isEnrolled = (course) => {
  // 【修正】使用 course.courseId 来判断是否已选
  return enrolledCourseIds.value.has(course.courseId);
};

/**
 * 判断课程是否时间冲突
 * 【修正】只在“未选”的课程里判断冲突，避免和自身冲突
 * @param {object} course - 完整的课程对象
 */
const hasConflict = (course) => {
  // 如果课程已选，我们不把它标记为冲突
  if (isEnrolled(course)) return false;

  // 如果课程没有安排时间，不认为有冲突
  if (!course.courseDay || !course.courseTime) {
    return false;
  }
  // 检查是否与已选课程的时间发生冲突
  return mySchedule.value.has(`${course.courseDay}-${course.courseTime}`);
};

/**
 * 统一的按钮禁用逻辑
 */
const isButtonDisabled = (course) => {
  if (enrollingId.value === course.id) return true; // 正在处理中，禁用
  if (isEnrolled(course)) return true; // 已选，禁用
  if (hasConflict(course)) return true; // 冲突，禁用
  return false;
};

const handleEnroll = async (courseId) => {
  enrollingId.value = courseId; // 设置正在处理的课程ID
  try {
    await enrollmentService.enrollInCourse(courseId);
    showNotification('选课成功！', 'success');
    await fetchData(); // 重新加载数据以更新状态
  } catch (error) {
    showNotification(error.message || '选课失败', 'error');
  } finally {
    enrollingId.value = null; // 恢复处理状态
  }
};
</script>

<style scoped>
@import '@/assets/styles/common-page.css';

.search-panel {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  padding: 16px 24px;
  margin-bottom: 24px;
}
.search-item {
  display: flex;
  flex-direction: column;
}
.search-item label {
  font-size: 0.9em;
  color: var(--color-text-secondary);
  margin-bottom: 4px;
}
.search-item input {
  padding: 8px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  font-size: 1em;
}
.search-actions {
  display: flex;
  gap: 8px;
}
.search-btn, .reset-btn {
  padding: 8px 20px;
  border-radius: var(--border-radius);
  border: none;
  cursor: pointer;
}
.search-btn {
  background-color: var(--color-primary);
  color: white;
}
.reset-btn {
  background-color: #f1f3f5;
  color: var(--color-text-secondary);
}

.enroll-btn {
  background-color: var(--color-success);
  color: white;
  border: none;
  padding: 6px 16px;
  font-size: 0.9em;
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: all 0.2s;
}
.enroll-btn:hover:not(:disabled) {
  opacity: 0.9;
}
.enroll-btn:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
  opacity: 0.7;
}
</style>