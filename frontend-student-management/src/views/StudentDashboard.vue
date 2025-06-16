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
        请从左侧选择一个学生或新增学生
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
  height: 100%; /* 占满父容器高度 */
  width: 100%;
}
.student-list-panel {
  width: 300px;
  border-right: 1px solid #e0e0e0;
  overflow-y: auto;
  background-color: #fff;
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