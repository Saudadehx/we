<template>
  <div class="detail-container">
    <div class="header">
      <h3>{{ student.name }}的详细信息</h3>
      <div>
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
        <input v-else type="text" v-model="editableStudent.studentId">
      </div>
      <div class="info-item">
        <label>专业</label>
        <span v-if="!isEditing">{{ editableStudent.major }}</span>
        <input v-else type="text" v-model="editableStudent.major">
      </div>
      <div class="info-item">
        <label>班级</label>
        <span v-if="!isEditing">{{ editableStudent.className }}</span>
        <input v-else type="text" v-model="editableStudent.className">
      </div>
      <div class="info-item">
        <label>绩点 (GPA)</label>
        <span v-if="!isEditing">{{ editableStudent.gpa }}</span>
        <input v-else type="number" step="0.01" v-model="editableStudent.gpa">
      </div>
    </div>
  </div>
</template>

<script setup>
import { studentService, authService } from '@/services/apiService';
import { ref, watch, computed } from 'vue';
import { showNotification } from '@/services/notificationStore.js';

const props = defineProps({
  student: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['student-updated']);

const isEditing = ref(false);
const editableStudent = ref({ ...props.student });
const isAdmin = computed(() => authService.getUserRole() === 'ADMIN');
const errorMessage = ref('');
const fieldErrors = ref({});

// 监听 props.student 的变化，当父组件选择新学生时更新表单
watch(() => props.student, (newStudent) => {
  editableStudent.value = { ...newStudent };
  isEditing.value = false; // 切换学生时退出编辑模式
}, { immediate: true });

const saveChanges = async () => {
  errorMessage.value = '';
  fieldErrors.value = {};
  try {
    await studentService.updateStudent(props.student.id, editableStudent.value);
    isEditing.value = false;
    emit('student-updated'); // 通知父组件更新成功
    showNotification('更新成功！', 'success');
  } catch (error) {
    if (error.response && error.response.data) {
      const errorData = error.response.data;
      if (typeof errorData === 'object') {
        // 如果后端返回的是字段错误对象，例如 { "name": "姓名太短" }
        fieldErrors.value = errorData; // <--- 将错误对象存起来
        errorMessage.value = "请修正标记为红色的字段。";
      } else {
        errorMessage.value = errorData;
      }
    } else {
      showNotification(Object.values(error.response.data).join('; ') || '未知错误');
    }
  }
};

const cancelEdit = () => {
  editableStudent.value = { ...props.student }; // 恢复原始数据
  isEditing.value = false;
};
</script>

<style scoped>
.detail-container {
  padding: 1rem;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2px solid #337ab7;
  padding-bottom: 10px;
  margin-bottom: 20px;
}
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}
.info-item {
  display: flex;
  flex-direction: column;
}
.info-item label {
  font-size: 0.9em;
  color: #888;
  margin-bottom: 5px;
}
.info-item span {
  font-size: 1.1em;
  padding: 8px;
  background-color: #f9f9f9;
  border-radius: 4px;
}
.info-item input {
  font-size: 1.1em;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.error-message {
  color: #D8000C; /* 红色字体 */
  background-color: #FFD2D2; /* 淡红色背景 */
  padding: 10px;
  border-radius: 4px;
  margin-top: 15px;
  text-align: center;
}
.input-error {
  border: 1px solid #D8000C !important; /* 用红色边框高亮 */
  box-shadow: 0 0 5px rgba(216, 0, 12, 0.3);
}
button {
  margin-left: 10px;
}
.save-btn { background-color: #198754; color: white; }
.cancel-btn { background-color: #6c757d; color: white; }
</style>