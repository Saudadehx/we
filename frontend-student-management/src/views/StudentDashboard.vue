<template>
  <div class="dashboard-container">
    <div class="student-list-panel">
      <StudentList
          :students="students"
          @student-selected="handleStudentSelect"
          @add-student-requested="handleAddNewStudent"
      />
    </div>

    <div class="student-detail-panel">
      <StudentDetail
          v-if="selectedStudent"
          :student="selectedStudent"
          :key="selectedStudent.id || 'new-student-form'"
          @create-student="handleCreateStudent"
          @update-student="handleUpdateStudent"
          @delete-student="handleDeleteStudent"
      />
      <div v-else class="placeholder">
        <p>请从左侧选择学生以查看详情</p>
      </div>
    </div>
  </div>
</template>

<script setup>
// 这个文件的 script 部分
import { ref, onMounted } from 'vue';
import { studentService } from '@/services/apiService';
import { showNotification } from '@/services/notificationStore.js';
import StudentList from '@/components/StudentList.vue';
import StudentDetail from '@/components/StudentDetail.vue';

const students = ref([]);
const selectedStudent = ref(null);

const fetchStudents = async () => {
  try {
    students.value = await studentService.getAllStudents();
  } catch (error) {
    showNotification('获取学生列表失败', 'error');
  }
};
onMounted(fetchStudents);

const handleStudentSelect = (student) => {
  selectedStudent.value = student;
};

const handleAddNewStudent = () => {
  const newStudentTemplate = {
    studentId: '', name: '', gender: '男', dateOfBirth: '', className: '', major: '', gpa: null, photoUrl: ''
  };
  selectedStudent.value = newStudentTemplate;
};

const handleCreateStudent = async (formData) => {
  try {
    const newStudent = await studentService.createStudent(formData);
    showNotification('学生创建成功！', 'success');
    await fetchStudents();
    const created = students.value.find(s => s.studentId === newStudent.studentId);
    selectedStudent.value = created || null;
  } catch (error) {
    showNotification(`创建失败: ${error.message}`, 'error');
  }
};

const handleUpdateStudent = async (formData) => {
  try {
    const updatedStudent = await studentService.updateStudent(formData.id, formData);
    showNotification('学生信息更新成功！', 'success');
    await fetchStudents();
    selectedStudent.value = updatedStudent;
  } catch (error) {
    showNotification(`保存失败: ${error.message}`, 'error');
    await fetchStudents();
  }
};

const handleDeleteStudent = async (studentId) => {
  if (window.confirm('确定要删除这位学生吗？此操作不可撤销。')) {
    try {
      await studentService.deleteStudent(studentId);
      showNotification('学生已删除。', 'success');
      selectedStudent.value = null;
      await fetchStudents();
    } catch (error) {
      showNotification(`删除失败: ${error.message}`, 'error');
    }
  }
};
</script>

<style scoped>
/* 样式保持不变 */
.dashboard-container { display: flex; height: 100%; width: 100%; }
.student-list-panel { width: 300px; border-right: 1px solid #e0e0e0; background-color: #fff; display: flex; flex-direction: column; }
.student-detail-panel { flex-grow: 1; display: flex; flex-direction: column; }
.placeholder { display: flex; flex-direction: column; justify-content: center; align-items: center; height: 100%; color: #b0bec5; }
.placeholder p { font-size: 1.5em; margin: 0; color: #78909c; }
</style>