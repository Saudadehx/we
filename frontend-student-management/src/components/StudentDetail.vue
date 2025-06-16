<template>
  <div class="detail-container" v-if="editableStudent">
    <div class="header">
      <div class="header-info">
        <img :src="editableStudent.photoUrl || 'default-avatar.png'" alt="avatar" class="header-avatar">
        <div>
          <h3>{{ headerText }}</h3>
          <p class="header-subtitle" v-if="!isCreating">{{ editableStudent.studentId }}</p>
        </div>
      </div>

      <div class="header-actions">
        <button v-if="isAdmin && !isCreating && !isEditing" class="action-btn edit-btn" @click="isEditing = true">编辑</button>
        <button v-if="isAdmin && !isCreating && !isEditing" class="action-btn delete-btn" @click="confirmDelete">删除</button>
        <button v-if="isEditing" class="action-btn save-btn" @click="saveChanges">保存</button>
        <button v-if="isEditing" class="action-btn cancel-btn" @click="cancelEdit">取消</button>
      </div>
    </div>

    <div v-if="isEditing && errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <div class="content-grid">
      <div class="info-card">
        <h4>基本信息</h4>
        <div class="info-item">
          <label>姓名</label>
          <span v-if="!isEditing">{{ editableStudent.name }}</span>
          <input v-else type="text" v-model="editableStudent.name" :class="{ 'input-error': fieldErrors.name }">
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
      </div>

      <div class="info-card">
        <h4>学籍信息</h4>
        <div class="info-item">
          <label>学号</label>
          <span v-if="!isEditing">{{ editableStudent.studentId }}</span>
          <input v-else type="text" v-model="editableStudent.studentId" :class="{ 'input-error': fieldErrors.studentId }">
        </div>
        <div class="info-item">
          <label>班级</label>
          <span v-if="!isEditing">{{ editableStudent.className }}</span>
          <input v-else type="text" v-model="editableStudent.className" :class="{ 'input-error': fieldErrors.className }">
        </div>
        <div class="info-item">
          <label>专业</label>
          <span v-if="!isEditing">{{ editableStudent.major }}</span>
          <input v-else type="text" v-model="editableStudent.major" :class="{ 'input-error': fieldErrors.major }">
        </div>
        <div class="info-item">
          <label>绩点 (GPA)</label>
          <span v-if="!isEditing">{{ editableStudent.gpa }}</span>
          <input v-else type="number" step="0.01" v-model="editableStudent.gpa" :class="{ 'input-error': fieldErrors.gpa }">
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue';
import { studentService, authService } from '@/services/apiService';
import { showNotification } from '@/services/notificationStore.js';

const props = defineProps({
  student: { type: Object, default: null },
  isCreating: { type: Boolean, default: false }
});

const emit = defineEmits(['student-updated', 'student-created', 'student-deleted']);

const isEditing = ref(props.isCreating);
const editableStudent = ref(null);
const errorMessage = ref('');
const fieldErrors = ref({});

const isAdmin = computed(() => authService.getUserRole() === 'ADMIN');
const headerText = computed(() => {
  if (props.isCreating) return '新增学生';
  return props.student ? props.student.name : '详细信息';
});

const resetForm = () => {
  if (props.isCreating) {
    editableStudent.value = { name: '', studentId: '', gender: '男', dateOfBirth: new Date().toISOString().split('T')[0], major: '', className: '', gpa: 0.0, photoUrl: '' };
    isEditing.value = true;
  } else {
    editableStudent.value = props.student ? { ...props.student } : null;
    isEditing.value = false;
  }
};

watch(() => [props.student, props.isCreating], resetForm, { immediate: true });

const saveChanges = async () => {
  errorMessage.value = '';
  fieldErrors.value = {};
  try {
    if (props.isCreating) {
      const response = await studentService.createStudent(editableStudent.value);
      emit('student-created', response.data);
      showNotification('学生创建完成！', 'success');
    } else {
      await studentService.updateStudent(props.student.id, editableStudent.value);
      isEditing.value = false;
      emit('student-updated');
      showNotification('保存完成！', 'success');
    }
  } catch (error) {
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
  if (props.isCreating) {
    // 如果是创建中取消，可以考虑通知父组件关闭创建模式
  } else {
    resetForm();
    errorMessage.value = '';
    fieldErrors.value = {};
  }
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
.detail-container {
  padding: 2rem;
  height: 100%;
  overflow-y: auto;
}

/* 头部样式 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e0e0e0;
}
.header-info {
  display: flex;
  align-items: center;
}
.header-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  margin-right: 1.5rem;
}
.header-info h3 {
  margin: 0;
  font-size: 1.8em;
  color: #333;
}
.header-subtitle {
  margin: 0;
  color: #777;
}

/* 按钮样式 */
.header-actions .action-btn {
  margin-left: 10px;
  border: none;
  padding: 8px 18px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95em;
  font-weight: 500;
  transition: all 0.2s ease;
}
.action-btn.edit-btn { background-color: #337ab7; color: white; }
.action-btn.delete-btn { background-color: #f5f5f5; color: #c82333; border: 1px solid #c82333;}
.action-btn.save-btn { background-color: #28a745; color: white; }
.action-btn.cancel-btn { background-color: #6c757d; color: white; }
.action-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

/* 错误提示 */
.error-message {
  color: #D8000C;
  background-color: #FFD2D2;
  padding: 10px;
  border-radius: 4px;
  margin: 1rem 0;
  text-align: center;
}

/* 内容网格和卡片布局 */
.content-grid {
  margin-top: 2rem;
  display: grid;
  grid-template-columns: 1fr; /* 单列布局，让卡片纵向排列 */
  gap: 1.5rem;
}
.info-card {
  background-color: #fff;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 1.5rem;
}
.info-card h4 {
  margin-top: 0;
  margin-bottom: 1.5rem;
  font-size: 1.2em;
  border-bottom: 1px solid #eee;
  padding-bottom: 0.75rem;
}

/* 表单项样式 */
.info-item {
  display: flex;
  flex-direction: column;
  margin-bottom: 1.2rem;
}
.info-item:last-child {
  margin-bottom: 0;
}
.info-item label {
  font-size: 0.9em;
  color: #888;
  margin-bottom: 5px;
}
.info-item input, .info-item select {
  font-size: 1em;
  padding: 10px;
  border-radius: 6px;
  border: 1px solid #ddd;
  background-color: #fdfdfd;
  font-family: inherit; /* 继承父级字体 */
}
.info-item span {
  font-size: 1em;
  padding: 10px 0; /* 调整 span 的内边距以对齐 */
}
.info-item select {
  -webkit-appearance: none; /* 移除 Chrome/Safari 的默认样式 */
  -moz-appearance: none; /* 移除 Firefox 的默认样式 */
  appearance: none; /* 移除标准浏览器默认样式 */
  /* 使用内联 SVG 创建一个自定义的下拉箭头，并放置在背景中 */
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' fill='%23888888' viewBox='0 0 16 16'%3E%3Cpath fill-rule='evenodd' d='M1.646 4.646a.5.5 0 0 1 .708 0L8 10.293l5.646-5.647a.5.5 0 0 1 .708.708l-6 6a.5.5 0 0 1-.708 0l-6-6a.5.5 0 0 1 0-.708z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 10px center;
  background-size: 16px;
  padding-right: 35px; /* 为箭头图标留出空间 */
}
.input-error {
  border-color: #c82333 !important;
  box-shadow: 0 0 5px rgba(216, 0, 12, 0.2);
}
.input-error {
  border-color: #c82333 !important;
  box-shadow: 0 0 5px rgba(216, 0, 12, 0.2);
}
</style>