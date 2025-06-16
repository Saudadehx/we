<template>
  <div class="detail-container">
    <div class="header">
      <h3>{{ headerText }}</h3>
      <div>
        <button v-if="isAdmin && !isCreating && !isEditing" class="delete-btn" @click="confirmDelete">删除</button>
        <button v-if="isAdmin && !isEditing" @click="isEditing = true">编辑</button>
        <button v-if="isEditing" class="save-btn" @click="saveChanges">保存</button>
        <button v-if="isEditing" class="cancel-btn" @click="cancelEdit">取消</button>
      </div>
    </div>

    <div v-if="isEditing && errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <div class="info-grid">
      <div class="info-item">
        <label>姓名</label>
        <span v-if="!isEditing">{{ editableStudent.name }}</span>
        <input v-else type="text" v-model="editableStudent.name" :class="{ 'input-error': fieldErrors.name }">
      </div>
      <div class="info-item">
        <label>学号</label>
        <span v-if="!isEditing">{{ editableStudent.studentId }}</span>
        <input v-else type="text" v-model="editableStudent.studentId" :class="{ 'input-error': fieldErrors.studentId }">
      </div>
      <div class="info-item">
        <label>性别</label>
        <span v-if="!isEditing">{{ editableStudent.gender }}</span>
        <select v-else v-model="editableStudent.gender">
          <option>男</option>
          <option>女</option>
          <option>其他</option>
        </select>
      </div>
      <div class="info-item">
        <label>出生日期</label>
        <span v-if="!isEditing">{{ editableStudent.dateOfBirth }}</span>
        <input v-else type="date" v-model="editableStudent.dateOfBirth">
      </div>
      <div class="info-item">
        <label>专业</label>
        <span v-if="!isEditing">{{ editableStudent.major }}</span>
        <input v-else type="text" v-model="editableStudent.major" :class="{ 'input-error': fieldErrors.major }">
      </div>
      <div class="info-item">
        <label>班级</label>
        <span v-if="!isEditing">{{ editableStudent.className }}</span>
        <input v-else type="text" v-model="editableStudent.className" :class="{ 'input-error': fieldErrors.className }">
      </div>
      <div class="info-item">
        <label>绩点 (GPA)</label>
        <span v-if="!isEditing">{{ editableStudent.gpa }}</span>
        <input v-else type="number" step="0.01" v-model="editableStudent.gpa" :class="{ 'input-error': fieldErrors.gpa }">
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted } from 'vue';
import { studentService, authService } from '@/services/apiService';
import { showNotification } from '@/services/notificationStore.js';

const props = defineProps({
  student: {
    type: Object,
    default: null
  },
  isCreating: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['student-updated', 'student-created', 'student-deleted']);

const isEditing = ref(props.isCreating);
const editableStudent = ref({});
const errorMessage = ref('');
const fieldErrors = ref({});

const isAdmin = computed(() => authService.getUserRole() === 'ADMIN');
const headerText = computed(() => {
  if (props.isCreating) return '新增学生信息';
  return props.student ? `${props.student.name}的详细信息` : '详细信息';
});

const resetForm = () => {
  if (props.isCreating) {
    editableStudent.value = {
      name: '', studentId: '', gender: '男', dateOfBirth: new Date().toISOString().split('T')[0], major: '', className: '', gpa: 0.0
    };
    isEditing.value = true;
  } else {
    editableStudent.value = { ...props.student };
    isEditing.value = false;
  }
};

watch(() => [props.student, props.isCreating], resetForm, { immediate: true });

const saveChanges = async () => {
  errorMessage.value = '';
  fieldErrors.value = {};

  try {
    if (props.isCreating) {
      // 创建新学生
      const response = await studentService.createStudent(editableStudent.value);
      emit('student-created', response.data);
      showNotification('学生创建成功！', 'success');
    } else {
      // 更新现有学生
      await studentService.updateStudent(props.student.id, editableStudent.value);
      emit('student-updated');
      showNotification('更新成功！', 'success');
    }
  } catch (error) {
    console.error('保存失败:', error);
    if (error.response && error.response.data) {
      const errorData = error.response.data;
      if (typeof errorData === 'object' && Object.keys(errorData).length > 0) {
        fieldErrors.value = errorData;
        const firstErrorKey = Object.keys(errorData)[0];
        errorMessage.value = errorData[firstErrorKey];
      } else {
        errorMessage.value = errorData.toString();
      }
      showNotification('保存失败，请检查表单。', 'error');
    } else {
      showNotification('发生未知网络错误。', 'error');
    }
  }
};

const cancelEdit = () => {
  resetForm();
  errorMessage.value = '';
  fieldErrors.value = {};
};

const confirmDelete = async () => {
  if (window.confirm(`确定要删除学生 ${props.student.name} 吗？此操作不可撤销。`)) {
    try {
      await studentService.deleteStudent(props.student.id);
      showNotification('学生已删除。', 'success');
      emit('student-deleted');
    } catch (err) {
      showNotification('删除失败，请稍后再试。', 'error');
    }
  }
};

</script>

<style scoped>
/* ... 其他样式保持不变 ... */
.detail-container { padding: 1rem; }
.header { display: flex; justify-content: space-between; align-items: center; border-bottom: 2px solid #337ab7; padding-bottom: 10px; margin-bottom: 20px; }
.info-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }
.info-item { display: flex; flex-direction: column; }
.info-item label { font-size: 0.9em; color: #888; margin-bottom: 5px; }
.info-item span, .info-item input, .info-item select { font-size: 1.1em; padding: 8px; border-radius: 4px; border: 1px solid #ddd; }
.info-item span { background-color: #f9f9f9; }
button { margin-left: 10px; border: none; padding: 8px 15px; border-radius: 4px; cursor: pointer; }
.save-btn { background-color: #198754; color: white; }
.cancel-btn { background-color: #6c757d; color: white; }
.delete-btn { background-color: #dc3545; color: white; }
.error-message { color: #D8000C; background-color: #FFD2D2; padding: 10px; border-radius: 4px; margin-bottom: 15px; text-align: center; }
.input-error { border: 1px solid #D8000C !important; box-shadow: 0 0 5px rgba(216, 0, 12, 0.3); }
</style>