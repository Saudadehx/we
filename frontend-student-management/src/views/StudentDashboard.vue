<template>
  <div class="dashboard-container">
    <div class="student-list-panel">
      <StudentList
          :students="students"
          :selected-student-id="selectedStudent ? selectedStudent.id : null"
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
        <div class="placeholder-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="96" height="96" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
            <circle cx="9" cy="7" r="4"></circle>
            <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
          </svg>
        </div>
        <h3 class="placeholder-title">查看学生详情</h3>
        <p class="placeholder-text">请从左侧列表中选择一位学生，<br>或点击“新增”按钮来创建一份新的学生档案。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
// Script部分无需修改
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
.dashboard-container {
  display: flex;
  height: 100%;
  width: 100%;
}
.student-list-panel {
  width: 300px;
  flex-shrink: 0; /* 防止列表面板被压缩 */
}
.student-detail-panel {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}
.placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  text-align: center;
  color: var(--color-text-secondary); /* 使用全局变量 */
  padding: var(--spacing-xl); /* 使用全局变量 */
  box-sizing: border-box;
}

.placeholder-icon {
  color: var(--color-border); /* 使用一个柔和的颜色 */
  margin-bottom: var(--spacing-lg);
}

.placeholder-title {
  font-size: 1.5em;
  font-weight: 600;
  color: var(--color-text-primary); /* 使用更深的文字颜色 */
  margin: 0 0 var(--spacing-sm) 0;
}

.placeholder-text {
  max-width: 400px;
  line-height: var(--line-height-base);
  margin: 0;
}
</style>