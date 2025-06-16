<template>
  <div class="detail-container" v-if="student">
    <div class="header">
      <div class="header-info">
        <h3>{{ isCreatingNew ? '新增学生档案' : editableStudent.name }}</h3>
        <p class="header-subtitle">{{ isCreatingNew ? '请填写以下信息' : editableStudent.studentId }}</p>
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
        <div class="form-grid">
          <div class="info-item">
            <label for="name">姓名</label>
            <span v-if="!isEditing">{{ student.name }}</span>
            <input v-else v-model="editableStudent.name" id="name" class="editable-input"/>
          </div>
          <div class="info-item">
            <label for="gender">性别</label>
            <span v-if="!isEditing">{{ student.gender }}</span>
            <select v-else v-model="editableStudent.gender" id="gender" class="editable-input">
              <option>男</option><option>女</option><option>其他</option>
            </select>
          </div>
          <div class="info-item">
            <label for="dob">出生日期</label>
            <span v-if="!isEditing">{{ student.dateOfBirth }}</span>
            <input v-else type="date" v-model="editableStudent.dateOfBirth" id="dob" class="editable-input" />
          </div>
          <div class="info-item">
            <label for="ethnicity">民族</label>
            <span v-if="!isEditing">{{ student.ethnicity }}</span>
            <input v-else v-model="editableStudent.ethnicity" id="ethnicity" class="editable-input" />
          </div>
          <div class="info-item">
            <label for="nativePlace">籍贯</label>
            <span v-if="!isEditing">{{ student.nativePlace }}</span>
            <input v-else v-model="editableStudent.nativePlace" id="nativePlace" class="editable-input" />
          </div>
          <div class="info-item">
            <label for="politicalStatus">政治面貌</label>
            <span v-if="!isEditing">{{ student.politicalStatus }}</span>
            <select v-else v-model="editableStudent.politicalStatus" id="politicalStatus" class="editable-input">
              <option>群众</option><option>共青团员</option><option>中共预备党员</option><option>中共党员</option><option>其他</option>
            </select>
          </div>
        </div>
      </div>

      <div class="info-card">
        <h4>联系方式</h4>
        <div class="form-grid">
          <div class="info-item">
            <label for="phoneNumber">手机号码</label>
            <span v-if="!isEditing">{{ student.phoneNumber }}</span>
            <input v-else v-model="editableStudent.phoneNumber" id="phoneNumber" class="editable-input" />
          </div>
          <div class="info-item">
            <label for="email">电子邮箱</label>
            <span v-if="!isEditing">{{ student.email }}</span>
            <input v-else type="email" v-model="editableStudent.email" id="email" class="editable-input" />
          </div>
        </div>
      </div>

      <div class="info-card">
        <h4>学籍信息</h4>
        <div class="form-grid">
          <div class="info-item">
            <label for="studentId">学号</label>
            <span v-if="!isEditing">{{ student.studentId }}</span>
            <input v-else v-model="editableStudent.studentId" id="studentId" class="editable-input" :disabled="!isCreatingNew"/>
          </div>
          <div class="info-item">
            <label for="enrollmentDate">入学日期</label>
            <span v-if="!isEditing">{{ student.enrollmentDate }}</span>
            <input v-else type="date" v-model="editableStudent.enrollmentDate" id="enrollmentDate" class="editable-input" />
          </div>
          <div class="info-item">
            <label for="college">学院</label>
            <span v-if="!isEditing">{{ student.college }}</span>
            <input v-else v-model="editableStudent.college" id="college" class="editable-input" />
          </div>
          <div class="info-item">
            <label for="major">专业</label>
            <span v-if="!isEditing">{{ student.major }}</span>
            <input v-else v-model="editableStudent.major" id="major" class="editable-input" />
          </div>
          <div class="info-item">
            <label for="className">班级</label>
            <span v-if="!isEditing">{{ student.className }}</span>
            <input v-else v-model="editableStudent.className" id="className" class="editable-input" />
          </div>
          <div class="info-item">
            <label for="studentStatus">学籍状态</label>
            <span v-if="!isEditing">{{ student.studentStatus }}</span>
            <select v-else v-model="editableStudent.studentStatus" id="studentStatus" class="editable-input">
              <option>在读</option><option>休学</option><option>毕业</option><option>退学</option>
            </select>
          </div>
          <div class="info-item">
            <label for="gpa">绩点 (GPA)</label>
            <span v-if="!isEditing">{{ student.gpa }}</span>
            <input v-else type="number" step="0.01" v-model="editableStudent.gpa" id="gpa" class="editable-input" />
          </div>
        </div>
      </div>

      <div class="info-card" v-if="isEditing">
        <h4>账户信息</h4>
        <div class="form-grid">
          <div class="info-item">
            <label for="password">登录密码</label>
            <input type="password" v-model="editableStudent.password" id="password" class="editable-input" placeholder="不修改请留空"/>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// Script 部分保持不变
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
.detail-container {
  padding: var(--spacing-xl);
  overflow-y: auto;
  height: 100%;
  box-sizing: border-box;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: var(--spacing-lg);
  border-bottom: 1px solid var(--color-border);
  margin-bottom: var(--spacing-xl);
}
.header-info h3 {
  margin: 0;
  font-size: 1.8em;
  color: var(--color-text-primary);
}
.header-subtitle {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 1em;
}
.header-actions {
  display: flex;
  gap: var(--spacing-md);
}
.action-btn {
  border: none;
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--border-radius);
  cursor: pointer;
  font-weight: 500;
  transition: all var(--transition-speed) ease;
}
.action-btn.edit-btn { background-color: var(--color-primary-light); color: var(--color-primary); }
.action-btn.delete-btn { background-color: #fdf2f2; color: var(--color-danger); }
.action-btn.save-btn { background-color: var(--color-success); color: white; }
.action-btn.cancel-btn { background-color: var(--color-text-secondary); color: white; }
.action-btn:hover { opacity: 0.85; }

.content-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--spacing-xl);
}

.info-card {
  background-color: var(--color-surface);
  border-radius: var(--border-radius);
  padding: var(--spacing-lg);
  box-shadow: var(--box-shadow);
}
.info-card h4 {
  margin-top: 0;
  margin-bottom: var(--spacing-lg);
  font-size: 1.2em;
  color: var(--color-text-primary);
  border-bottom: 1px solid var(--color-border);
  padding-bottom: var(--spacing-md);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr); /* 创建两列 */
  gap: var(--spacing-md) var(--spacing-xl); /* 行间距 和 列间距 */
}

.info-item {
  display: grid;
  grid-template-columns: 80px 1fr; /* 标签80px宽，输入框占满剩余 */
  align-items: center;
  gap: var(--spacing-md);
}

.info-item:last-child {
  margin-bottom: 0;
}
.info-item label {
  color: var(--color-text-secondary);
  font-weight: 500;
  text-align: right;
  font-size: 0.9em;
}
.info-item span {
  font-weight: 500;
  color: var(--color-text-primary);
}

/* 统一的输入框样式 */
.editable-input {
  font-family: inherit;
  font-size: 1em;
  padding: 10px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  background-color: var(--color-surface);
  outline: none;
  transition: all var(--transition-speed) ease;
  width: 100%;
  box-sizing: border-box;
}
.editable-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-light);
}
.editable-input.input-error {
  border-color: var(--color-danger) !important;
}
.editable-input.input-error:focus {
  box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.15);
}
.editable-input:disabled {
  background-color: #f5f7fa;
  cursor: not-allowed;
}

.select-wrapper {
  position: relative;
}
select.editable-input {
  appearance: none;
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16'%3e%3cpath fill='none' stroke='%23343a40' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M2 5l6 6 6-6'/%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 0.75rem center;
  background-size: 16px 12px;
  padding-right: 2.5rem;
}
</style>