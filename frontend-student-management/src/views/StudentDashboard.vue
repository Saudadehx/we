<template>
  <div class="dashboard-container">
    <div class="student-list-panel">
      <StudentList
          :students="students"
          @student-selected="handleStudentSelect"
          @add-student-requested="handleAddStudent"
      />
    </div>
    <div class="student-detail-panel">
      <StudentDetail
          v-if="selectedStudent || isCreating"
          :student="selectedStudent"
          :is-creating="isCreating"
          :key="selectedStudent ? selectedStudent.id : 'new'"
          @student-updated="refreshStudentList"
          @student-created="handleStudentCreated"
          @student-deleted="handleStudentDeleted"
      />
      <div v-else class="placeholder">
        <div class="placeholder-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="80" height="80" fill="currentColor" class="bi bi-people" viewBox="0 0 16 16">
            <path d="M15 14s1 0 1-1-1-4-5-4-5 3-5 4 1 1 1 1zm-7.978-1A.261.261 0 0 1 7 12.996c.001-.264.167-1.03.76-1.72C8.312 10.629 9.282 10 11 10c1.717 0 2.687.63 3.24 1.276.593.69.758 1.457.76 1.72l-.008.002a.274.274 0 0 1-.014.002H7.022ZM11 7a2 2 0 1 0 0-4 2 2 0 0 0 0 4m3-2a3 3 0 1 1-6 0 3 3 0 0 1 6 0M6.957 12.287c-.545.636-.99 1.17-1.255 1.65a.5.5 0 0 1-.415.263H2.5a.5.5 0 0 1 0-1h1.043c.101-.16.208-.322.321-.479L5 11.231l.008-.002A.274.274 0 0 1 5 11.23zM5 7a2 2 0 1 0 0-4 2 2 0 0 0 0 4m0-2a3 3 0 1 1-6 0 3 3 0 0 1 6 0"/>
          </svg>
        </div>
        <p>请从左侧选择学生以查看详情</p>
        <span>或点击左上角的“新增学生”按钮来创建档案</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { studentService } from '@/services/apiService';
import StudentList from '@/components/StudentList.vue';
import StudentDetail from '@/components/StudentDetail.vue';

const students = ref([]);
const selectedStudent = ref(null);
const isCreating = ref(false);

const fetchStudents = async () => {
  try {
    const response = await studentService.getAllStudents();
    students.value = response.data;
  } catch (error) {
    console.error('获取学生列表失败:', error);
  }
};

onMounted(fetchStudents);

const handleStudentSelect = (student) => {
  isCreating.value = false;
  selectedStudent.value = student;
};

const handleAddStudent = () => {
  selectedStudent.value = null; // 清空当前选择
  isCreating.value = true; // 进入创建模式
};

const refreshStudentList = async () => {
  isCreating.value = false;
  await fetchStudents();
};

const handleStudentCreated = async (newStudent) => {
  isCreating.value = false;
  await fetchStudents();
  // 创建成功后，自动选中这个新学生
  selectedStudent.value = newStudent;
};

const handleStudentDeleted = async () => {
  selectedStudent.value = null;
  isCreating.value = false;
  await fetchStudents();
};

</script>

<style scoped>
.dashboard-container {
  display: flex;
  height: 100%; /* 关键：占满父元素 .app-main 的全部高度 */
  width: 100%;
}
.student-list-panel {
  width: 300px;
  border-right: 1px solid #e0e0e0;
  background-color: #fff;

  /* 关键：让左侧面板成为一个flex容器，内部分配空间 */
  display: flex;
  flex-direction: column;
}
.student-detail-panel {
  flex-grow: 1;

  /* 关键：让右侧面板成为一个flex容器，内部分配空间 */
  display: flex;
  flex-direction: column;
}
.placeholder {
  display: flex;
  flex-direction: column; /* 改为垂直布局 */
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #b0bec5; /* 使用更柔和的颜色 */
  text-align: center;
  user-select: none; /* 文字不可选中 */
}
.placeholder-icon {
  margin-bottom: 2rem;
  color: #eceff1; /* 图标颜色更浅，作为背景装饰 */
}
.placeholder p {
  font-size: 1.5em;
  margin: 0;
  color: #78909c;
}
.placeholder span {
  font-size: 1em;
  margin-top: 0.5rem;
}
</style>