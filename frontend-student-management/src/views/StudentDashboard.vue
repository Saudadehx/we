<template>
  <div class="dashboard-container">
    <div class="student-list-panel">
      <StudentList :students="students" @student-selected="handleStudentSelect" />
    </div>
    <div class="student-detail-panel">
      <StudentDetail v-if="selectedStudent" :student="selectedStudent" :key="selectedStudent.id" @student-updated="refreshStudentList"/>
      <div v-else class="placeholder">
        请从左侧选择一个学生以查看详情
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

const fetchStudents = async () => {
  try {
    const response = await studentService.getAllStudents();
    students.value = response.data;
  } catch (error) {
    console.error('获取学生列表失败:', error);
  }
};

// 组件加载时获取学生列表
onMounted(fetchStudents);

// 当左侧列表发出 "student-selected" 事件时，此方法被调用
const handleStudentSelect = (student) => {
  selectedStudent.value = student;
};

// 当学生信息更新后，刷新列表以获取最新数据
const refreshStudentList = async () => {
  await fetchStudents();
};

</script>

<style scoped>
.dashboard-container {
  display: flex;
  height: calc(100vh - 150px); /* 视情况调整高度 */
}
.student-list-panel {
  width: 300px;
  border-right: 1px solid #e0e0e0;
  overflow-y: auto;
}
.student-detail-panel {
  flex-grow: 1;
  padding: 20px;
  overflow-y: auto;
}
.placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #888;
  font-size: 1.2em;
}
</style>