<template>
  <div class="detail-container" v-if="student">
    <div class="header">
      <div class="header-info">
        <div>
          <h3>{{ isCreatingNew ? '新增学生档案' : student.name }}</h3>
          <p class="header-subtitle">{{ isCreatingNew ? '请填写以下信息' : student.studentId }}</p>
        </div>
      </div>
      <div class="header-actions">
        <button v-if="isAdmin && !isEditing" class="action-btn edit-btn" @click="startEditing">编辑</button>
        <button v-if="isAdmin && !isEditing" class="action-btn delete-btn" @click="handleDelete">删除</button>
        <button v-if="isEditing" class="action-btn save-btn" @click="handleSave">保存</button>
        <button v-if="isEditing && !isCreatingNew" class="action-btn cancel-btn" @click="cancelEditing">取消</button>
      </div>
    </div>

    <div class="content-grid">
      <div class="info-card">
        <h4>基本信息</h4>
        <div class="info-item">
          <label>姓名</label>
          <span v-if="!isEditing">{{ student.name }}</span>
          <input v-else v-model="editableStudent.name" class="editable-input" :class="{ 'input-error': validationErrors.name }"/>
        </div>
        <div class="info-item">
          <label>性别</label>
          <span v-if="!isEditing">{{ student.gender }}</span>
          <div v-else class="select-wrapper">
            <select v-model="editableStudent.gender" class="editable-input">
              <option>男</option><option>女</option><option>其他</option>
            </select>
          </div>
        </div>
        <div class="info-item">
          <label>出生日期</label>
          <span v-if="!isEditing">{{ student.dateOfBirth }}</span>
          <input v-else type="date" v-model="editableStudent.dateOfBirth" class="editable-input" :class="{ 'input-error': validationErrors.dateOfBirth }" />
        </div>
      </div>

      <div class="info-card">
        <h4>学籍信息</h4>
        <div class="info-item">
          <label>学号</label>
          <span v-if="!isEditing">{{ student.studentId }}</span>
          <input v-else v-model="editableStudent.studentId" class="editable-input" :class="{ 'input-error': validationErrors.studentId }" :disabled="!isCreatingNew"/>
        </div>
        <div class="info-item">
          <label>班级</label>
          <span v-if="!isEditing">{{ student.className }}</span>
          <input v-else v-model="editableStudent.className" class="editable-input" :class="{ 'input-error': validationErrors.className }" />
        </div>
        <div class="info-item">
          <label>专业</label>
          <span v-if="!isEditing">{{ student.major }}</span>
          <input v-else v-model="editableStudent.major" class="editable-input" />
        </div>
        <div class="info-item">
          <label>绩点 (GPA)</label>
          <span v-if="!isEditing">{{ student.gpa }}</span>
          <input v-else type="number" step="0.01" v-model="editableStudent.gpa" class="editable-input" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// Script 部分使用我们最终调试好的版本，无需修改
import { ref, computed, watch } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { showNotification } from '@/services/notificationStore.js';

const props = defineProps({ student: { type: Object, required: true } });
const emit = defineEmits(['create-student', 'update-student', 'delete-student']);
const authStore = useAuthStore();
const isAdmin = computed(() => authStore.isAdmin);
const isEditing = ref(false);
const editableStudent = ref(null);
const validationErrors = ref({});
const isCreatingNew = computed(() => !props.student.id);

watch(() => props.student, (newStudent) => {
  if (newStudent) {
    editableStudent.value = { ...newStudent };
    validationErrors.value = {};
    isEditing.value = isCreatingNew.value;
  }
}, { immediate: true, deep: true });

const validateForm = () => {
  const errors = {};
  const form = editableStudent.value;
  if (!form.name || form.name.trim().length < 2) errors.name = '姓名长度至少为两个字符。';
  if (isCreatingNew.value && (!form.studentId || form.studentId.trim().length < 4)) errors.studentId = '学号不能为空且长度至少为4。';
  if (!form.className || form.className.trim() === '') errors.className = '班级名称不能为空。';
  if (!form.dateOfBirth) errors.dateOfBirth = '出生日期不能为空。';
  validationErrors.value = errors;
  if (Object.keys(errors).length > 0) {
    const firstError = Object.values(errors)[0];
    showNotification(firstError, 'error');
    return false;
  }
  return true;
};

const startEditing = () => { isEditing.value = true; };
const cancelEditing = () => {
  editableStudent.value = { ...props.student };
  validationErrors.value = {};
  isEditing.value = false;
};
const handleSave = () => {
  if (validateForm()) {
    if (isCreatingNew.value) {
      emit('create-student', editableStudent.value);
    } else {
      emit('update-student', editableStudent.value);
    }
  }
};
const handleDelete = () => { if (!isCreatingNew.value) emit('delete-student', props.student.id); };
</script>

<style scoped>
.detail-container { padding: 2rem; color: #333; }
.header { display: flex; justify-content: space-between; align-items: flex-start; padding-bottom: 1rem; border-bottom: 1px solid #e0e0e0; }
.header-info { display: flex; align-items: center; }
.header-info h3 { margin: 0; font-size: 1.8em; }
.header-subtitle { margin: 0; color: #777; }
.header-actions .action-btn { margin-left: 10px; border: none; padding: 8px 18px; border-radius: 6px; cursor: pointer; transition: all 0.2s; }
.content-grid { margin-top: 2rem; display: grid; grid-template-columns: 1fr; gap: 1.5rem; }
.info-card { background-color: #fff; border: 1px solid #e9ecef; border-radius: 8px; padding: 1.5rem; }
.info-card h4 { margin-top: 0; margin-bottom: 1.5rem; font-size: 1.2em; border-bottom: 1px solid #eee; padding-bottom: 0.75rem; }
.info-item { display: grid; grid-template-columns: 100px 1fr; margin-bottom: 1.5rem; align-items: center; min-height: 40px; }
.info-item label { color: #555; font-weight: 500;}
.info-item span { padding: 8px 10px; }
.action-btn.edit-btn { background-color: #337ab7; color: white; }
.action-btn.delete-btn { background-color: #f5f5f5; color: #c82333; border: 1px solid #c82333; }
.action-btn.save-btn { background-color: #28a745; color: white; }
.action-btn.cancel-btn { background-color: #6c757d; color: white; }
.action-btn:hover { opacity: 0.85; }

/* ✨✨✨ 全新的美化样式 ✨✨✨ */
.editable-input {
  font-family: inherit; font-size: 1em; padding: 8px 12px;
  border: 1px solid #ced4da; border-radius: 6px;
  background-color: #fff; outline: none;
  transition: all 0.2s ease-in-out; width: 100%;
}
.editable-input:focus {
  border-color: #86b7fe;
  box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);
}
.editable-input.input-error {
  border-color: #dc3545 !important;
}
.editable-input.input-error:focus {
  box-shadow: 0 0 0 0.25rem rgba(220, 53, 69, 0.25);
}
.select-wrapper { position: relative; width: 100%; }
.select-wrapper::after {
  content: '▼'; font-size: 0.7rem; position: absolute; right: 12px; top: 50%;
  transform: translateY(-50%); pointer-events: none; color: #888;
}
select.editable-input { -webkit-appearance: none; -moz-appearance: none; appearance: none; padding-right: 25px; }
input[type="date"].editable-input { padding: 7px 10px; }
</style>