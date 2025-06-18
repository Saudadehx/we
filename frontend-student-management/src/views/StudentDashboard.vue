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
          view-mode="admin"
          :is-saving="isSaving"
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
import { ref, onMounted } from 'vue';
import { studentService } from '@/services/apiService';
import { showNotification } from '@/services/notificationStore.js';
import StudentList from '@/components/StudentList.vue';
import StudentDetail from '@/components/StudentDetail.vue';

const students = ref([]);
const selectedStudent = ref(null);
const isSaving = ref(false); // ✨ 新增保存状态

const fetchStudents = async () => {
  try {
    // ✨ 后端现在返回 StudentDTO 列表
    students.value = await studentService.getAllStudents();
  } catch (error) {
    showNotification('获取学生列表失败', 'error');
  }
};
onMounted(fetchStudents);

const handleStudentSelect = (student) => {
  // ✨ 从列表选择时，确保拿到的是完整的 DTO
  studentService.getStudentById(student.id)
      .then(fullStudentData => {
        selectedStudent.value = fullStudentData;
      })
      .catch(err => showNotification('获取学生详细信息失败', 'error'));
};

const handleAddNewStudent = () => {
  // ✨ 创建一个符合新 StudentDTO 结构的模板对象
  const newStudentTemplate = {
    id: null,
    name: '',
    gender: '男',
    dateOfBirth: null,
    ethnicity: '',
    nativePlace: '',
    politicalStatus: '群众',
    phoneNumber: '',
    email: '',
    studentId: '',
    college: '',
    className: '',
    enrollmentDate: new Date().toISOString().split('T')[0], // 默认为当天
    studentStatus: '在读',
    gpa: null,
    photoUrl: '',
    majorId: null,
    academicYear: 1,
    semester: 1,
    majorName: '未分配', // 默认显示
    password: '' // 密码字段留空给表单填写
  };
  selectedStudent.value = newStudentTemplate;
};

const handleCreateStudent = async (formData) => {
  isSaving.value = true;
  try {
    const newStudent = await studentService.createStudent(formData);
    showNotification('学生创建成功！', 'success');
    await fetchStudents();
    // 创建成功后，直接将返回的、包含ID的新对象设为当前选中项
    selectedStudent.value = newStudent;
  } catch (error) {
    showNotification(`创建失败: ${error.message}`, 'error');
  } finally {
    isSaving.value = false;
  }
};

const handleUpdateStudent = async (formData) => {
  isSaving.value = true;
  try {
    const updatedStudent = await studentService.updateStudent(formData.id, formData);
    showNotification('学生信息更新成功！', 'success');
    // 更新左侧列表和右侧详情
    await fetchStudents();
    selectedStudent.value = updatedStudent;
  } catch (error) {
    showNotification(`保存失败: ${error.message}`, 'error');
  } finally {
    isSaving.value = false;
  }
};

const handleDeleteStudent = async (studentId) => {
  if (confirm('确定要删除这位学生吗？此操作不可撤销。')) {
    try {
      await studentService.deleteStudent(studentId);
      showNotification('学生已删除。', 'success');
      selectedStudent.value = null; // 清空右侧详情
      await fetchStudents(); // 重新加载左侧列表
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
  flex-shrink: 0;
}
.student-detail-panel {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  position: relative; /* 为 isSaving 遮罩做准备 */
}
.placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  text-align: center;
  color: var(--color-text-secondary);
  padding: var(--spacing-xl);
  box-sizing: border-box;
}
.placeholder-icon {
  color: var(--color-border);
  margin-bottom: var(--spacing-lg);
}
.placeholder-title {
  font-size: 1.5em;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 var(--spacing-sm) 0;
}
.placeholder-text {
  max-width: 400px;
  line-height: var(--line-height-base);
  margin: 0;
}
</style>