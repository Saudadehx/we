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
          <td>
            <span class="course-time-display" v-if="course.courseDay && course.courseTime">
              <svg class="time-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <polyline points="12 6 12 12 16 14"></polyline>
              </svg>
              {{ formatCourseTime(course.courseDay, course.courseTime) }}
            </span>
            <span v-else class="text-secondary">未安排</span>
          </td>
          <td class="action-col">
            <button
                @click="handleEnroll(course.id)"
                class="enroll-btn"
                :class="{
                  'btn-enrolled': isEnrolled(course),
                  'btn-conflict': hasConflict(course)
                }"
                :disabled="isButtonDisabled(course)"
            >
              <span v-if="enrollingId === course.id">处理中...</span>
              <span v-else-if="isEnrolled(course)">已选</span>
              <span v-else-if="hasConflict(course)">时间冲突</span>
              <span v-else>选课</span>
            </button>
          </td>
        </tr>
        <tr v-if="availableCourses.length === 0">
          <td colspan="6" class="no-data-cell">
            <div class="no-data-content">
              <svg class="no-data-icon" xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="8" y1="12" x2="16" y2="12"></line>
              </svg>
              <p>暂无课程可供选择或未找到符合条件的课程。</p>
            </div>
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
const enrollingId = ref(null); // 用于标记正在选课的课程ID

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

/* 搜索面板样式 */
.search-panel {
  display: flex;
  flex-wrap: wrap; /* 允许换行 */
  gap: 16px;
  align-items: flex-end;
  padding: 16px 24px;
  margin-bottom: 24px;
}
.search-item {
  display: flex;
  flex-direction: column;
  min-width: 180px; /* 确保搜索项不会太窄 */
  flex-grow: 1; /* 允许搜索项填充空间 */
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
  font-weight: 500;
  transition: all var(--transition-speed) ease;
}
.search-btn {
  background-color: var(--color-primary);
  color: white;
}
.reset-btn {
  background-color: #f1f3f5;
  color: var(--color-text-secondary);
}
.search-btn:hover, .reset-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

/* 表格样式美化 */
.available-courses-table {
  /* 增加表格圆角和阴影 */
  border-radius: var(--border-radius);
  overflow: hidden; /* 确保内容在圆角内 */
  box-shadow: var(--box-shadow);
}

.available-courses-table th, .available-courses-table td {
  padding: 14px 18px; /* 增加内边距 */
  vertical-align: middle;
}

.available-courses-table th {
  background-color: var(--color-background); /* 标题行使用更浅的背景色 */
  color: var(--color-text-primary);
  font-weight: 600;
  font-size: 0.95em;
  border-bottom: 1px solid var(--color-border);
}

.table-row {
  background-color: var(--color-surface);
  transition: background-color 0.2s ease;
}
.table-row:hover {
  background-color: var(--color-primary-light); /* 行hover效果 */
}
.table-row:last-child td {
  border-bottom: none; /* 最后一行无底部边框 */
}

/* 上课时间显示 */
.course-time-display {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--color-text-primary);
}
.time-icon {
  color: var(--color-primary);
}
.text-secondary {
  color: var(--color-text-secondary);
  font-style: italic;
}

/* 操作按钮和状态标签 */
.action-col {
  text-align: center;
  width: 120px; /* 确保操作列宽度一致 */
}

.enroll-btn {
  background-color: var(--color-primary);
  color: white;
  border: none;
  padding: 8px 18px;
  font-size: 0.9em;
  font-weight: 600;
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 90px; /* 确保按钮宽度一致 */
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.enroll-btn:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}
.enroll-btn:disabled {
  background-color: #e0e0e0; /* 禁用状态的背景色 */
  color: #a0a0a0; /* 禁用状态的文字颜色 */
  cursor: not-allowed;
  opacity: 0.8;
  box-shadow: none;
}

/* 针对不同状态的按钮美化 */
.enroll-btn.btn-enrolled {
  background-color: var(--color-primary-light);
  color: var(--color-primary);
  font-weight: 500;
  pointer-events: none; /* 完全禁用交互 */
}
.enroll-btn.btn-conflict {
  background-color: var(--color-warning);
  color: #fff;
  font-weight: 500;
  pointer-events: none; /* 完全禁用交互 */
}

/* 空数据提示 */
.no-data-cell {
  text-align: center;
  padding: 40px 20px;
  color: var(--color-text-secondary);
}
.no-data-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.no-data-icon {
  color: var(--color-border);
  opacity: 0.8;
}
.no-data-content p {
  margin: 0;
  font-size: 1.1em;
}
</style>