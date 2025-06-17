<template>
  <div class="detail-container" v-if="student">
    <div class="header">
      <div class="header-info">
        <h3 v-if="viewMode === 'admin'">{{ isCreatingNew ? '新增学生档案' : editableStudent.name }}</h3>
        <h3 v-else>我的个人档案</h3>

        <p class="header-subtitle" v-if="viewMode === 'admin'">{{ isCreatingNew ? '请填写以下信息' : editableStudent.studentId }}</p>
        <p class="header-subtitle" v-else>{{ student.name }} ({{ student.studentId }})</p>
      </div>

      <div class="header-actions">
        <template v-if="viewMode === 'admin'">
          <button v-if="isAdmin && !isEditing" class="action-btn edit-btn" @click="startEditing">编辑</button>
          <button v-if="isAdmin && !isEditing" class="action-btn delete-btn" @click="handleDelete">删除</button>
          <button v-if="isEditing" class="action-btn save-btn" @click="handleSave" :disabled="isSaving">
            <span v-if="isSaving">保存中...</span>
            <span v-else>保存</span>
          </button>
          <button v-if="isEditing && !isCreatingNew" class="action-btn cancel-btn" @click="cancelEditing">取消</button>
        </template>
        <template v-else-if="viewMode === 'student'">
          <button v-if="!isEditing" class="action-btn edit-btn" @click="startEditing">修改信息</button>
          <button v-if="isEditing" class="action-btn save-btn" @click="handleSave" :disabled="isSaving">
            <span v-if="isSaving">保存中...</span>
            <span v-else>保存更改</span>
          </button>
          <button v-if="isEditing" class="action-btn cancel-btn" @click="cancelEditing">取消</button>
        </template>
      </div>
    </div>

    <div class="content-grid">
      <div class="info-card">
        <h4>基本信息</h4>
        <div class="form-grid">
          <div class="info-item">
            <label>姓名</label>
            <span v-if="!isEditing || viewMode === 'student'">{{ student.name }}</span>
            <input v-else v-model="editableStudent.name" class="editable-input" :class="{ 'input-error': validationErrors.name }"/>
          </div>
          <div class="info-item">
            <label>性别</label>
            <span v-if="!isEditing || viewMode === 'student'">{{ student.gender }}</span>
            <select v-else v-model="editableStudent.gender" class="editable-input" :class="{ 'input-error': validationErrors.gender }">
              <option>男</option><option>女</option><option>其他</option>
            </select>
          </div>
          <div class="info-item">
            <label>出生日期</label>
            <span v-if="!isEditing || viewMode === 'student'">{{ student.dateOfBirth }}</span>
            <input v-else type="date" v-model="editableStudent.dateOfBirth" class="editable-input" :class="{ 'input-error': validationErrors.dateOfBirth }"/>
          </div>
          <div class="info-item">
            <label>民族</label>
            <span v-if="!isEditing || viewMode === 'student'">{{ student.ethnicity }}</span>
            <input v-else v-model="editableStudent.ethnicity" class="editable-input" />
          </div>
          <div class="info-item">
            <label>籍贯</label>
            <span v-if="!isEditing || viewMode === 'student'">{{ student.nativePlace }}</span>
            <input v-else v-model="editableStudent.nativePlace" class="editable-input" />
          </div>
          <div class="info-item">
            <label>政治面貌</label>
            <span v-if="!isEditing || viewMode === 'student'">{{ student.politicalStatus }}</span>
            <select v-else v-model="editableStudent.politicalStatus" class="editable-input">
              <option>群众</option><option>共青团员</option><option>中共预备党员</option><option>中共党员</option><option>其他</option>
            </select>
          </div>
        </div>
      </div>

      <div class="info-card">
        <h4>联系方式</h4>
        <div class="form-grid">
          <div class="info-item">
            <label>手机号码</label>
            <span v-if="!isEditing">{{ student.phoneNumber }}</span>
            <input v-else v-model="editableStudent.phoneNumber" class="editable-input" :class="{ 'input-error': validationErrors.phoneNumber }"/>
          </div>
          <div class="info-item">
            <label>电子邮箱</label>
            <span v-if="!isEditing">{{ student.email }}</span>
            <input v-else type="email" v-model="editableStudent.email" class="editable-input" :class="{ 'input-error': validationErrors.email }"/>
          </div>
        </div>
      </div>

      <div class="info-card">
        <h4>学籍信息</h4>
        <div class="form-grid">
          <div class="info-item">
            <label for="studentId">学号</label>
            <span v-if="!isEditing">{{ student.studentId }}</span>
            <input v-else v-model="editableStudent.studentId" id="studentId" class="editable-input" :disabled="!isCreatingNew" :class="{ 'input-error': validationErrors.studentId }"/>
          </div>
          <div class="info-item">
            <label>入学日期</label>
            <span v-if="!isEditing || viewMode === 'student'">{{ student.enrollmentDate }}</span>
            <input v-else type="date" v-model="editableStudent.enrollmentDate" class="editable-input" :class="{ 'input-error': validationErrors.enrollmentDate }"/>
          </div>
          <div class="info-item">
            <label>学院</label>
            <span v-if="!isEditing || viewMode === 'student'">{{ student.college }}</span>
            <input v-else v-model="editableStudent.college" class="editable-input" />
          </div>
          <div class="info-item">
            <label>专业</label>
            <span v-if="!isEditing || viewMode === 'student'">{{ student.major }}</span>
            <input v-else v-model="editableStudent.major" class="editable-input" />
          </div>
          <div class="info-item">
            <label>班级</label>
            <span v-if="!isEditing || viewMode === 'student'">{{ student.className }}</span>
            <input v-else v-model="editableStudent.className" class="editable-input" :class="{ 'input-error': validationErrors.className }"/>
          </div>
          <div class="info-item">
            <label>学籍状态</label>
            <span v-if="!isEditing || viewMode === 'student'">{{ student.studentStatus }}</span>
            <select v-else v-model="editableStudent.studentStatus" class="editable-input">
              <option>在读</option><option>休学</option><option>毕业</option><option>退学</option>
            </select>
          </div>
          <div class="info-item">
            <label>绩点 (GPA)</label>
            <span v-if="!isEditing || viewMode === 'student'">{{ student.gpa }}</span>
            <input v-else type="number" step="0.01" v-model="editableStudent.gpa" class="editable-input" />
          </div>
        </div>
      </div>

      <div class="info-card" v-if="isEditing">
        <h4>账户信息</h4>
        <div class="form-grid">
          <div class="info-item">
            <label for="password">重置密码</label>
            <input type="password" v-model="editableStudent.password" id="password" class="editable-input" placeholder="不修改请留空" :class="{ 'input-error': validationErrors.password }"/>
          </div>
          <div class="info-item" v-if="isCreatingNew">
            <label>初始密码</label>
            <input type="password" v-model="editableStudent.password" id="initial-password" class="editable-input" placeholder="请输入初始密码" required :class="{ 'input-error': validationErrors.password }"/>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { showNotification } from '@/services/notificationStore.js';

const props = defineProps({
  student: { type: Object, required: true },
  isAdmin: { type: Boolean, default: true },
  viewMode: { type: String, default: 'admin' }, // 'admin' 或 'student'
  isSaving: { type: Boolean, default: false } // 用于表示保存状态
});

const emit = defineEmits(['create-student', 'update-student', 'delete-student']);

const isEditing = ref(false);
const editableStudent = ref(null);
const validationErrors = ref({});
const isCreatingNew = computed(() => !props.student.id);

// 监听父组件传来的student对象，当它变化时，更新本地的可编辑对象
watch(() => props.student, (newStudent) => {
  if (newStudent) {
    // 复制一份数据用于编辑，避免直接修改父组件的props
    editableStudent.value = { ...newStudent, password: '' }; // 重置密码字段
    validationErrors.value = {};
    // 如果是新增学生，则自动进入编辑模式
    isEditing.value = isCreatingNew.value;
  }
}, { immediate: true, deep: true });

// 表单验证逻辑
const validateForm = () => {
  validationErrors.value = {}; // 清空之前的错误
  const form = editableStudent.value;

  let isValid = true;

  // 所有模式都应该验证的字段
  if (!form.name || form.name.trim().length < 2 || form.name.trim().length > 50) {
    validationErrors.value.name = '姓名不能为空且长度在2到50之间。';
    isValid = false;
  }
  if (!form.gender || form.gender.trim() === '') {
    validationErrors.value.gender = '性别不能为空。';
    isValid = false;
  }
  if (!form.dateOfBirth) {
    validationErrors.value.dateOfBirth = '出生日期不能为空。';
    isValid = false;
  } else {
    const dob = new Date(form.dateOfBirth);
    if (isNaN(dob) || dob > new Date()) {
      validationErrors.value.dateOfBirth = '出生日期必须是过去的时间。';
      isValid = false;
    }
  }
  if (form.phoneNumber && !/^1[3-9]\d{9}$/.test(form.phoneNumber)) {
    validationErrors.value.phoneNumber = '请输入有效的手机号码。';
    isValid = false;
  }
  if (form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    validationErrors.value.email = '请输入有效的电子邮箱。';
    isValid = false;
  }

  // 管理员模式或新增学生时特有的验证
  if (props.viewMode === 'admin' || isCreatingNew.value) {
    if (!form.studentId || form.studentId.trim().length < 4 || form.studentId.trim().length > 20) {
      validationErrors.value.studentId = '学号不能为空且长度在4到20之间。';
      isValid = false;
    }
    if (!form.className || form.className.trim().length === 0 || form.className.trim().length > 100) {
      validationErrors.value.className = '班级名称不能为空且长度不能超过100。';
      isValid = false;
    }
    if (isCreatingNew.value && (!form.password || form.password.trim() === '')) {
      validationErrors.value.password = '初始密码不能为空。';
      isValid = false;
    }
    if (form.enrollmentDate && new Date(form.enrollmentDate) > new Date()) {
      validationErrors.value.enrollmentDate = '入学日期不能是未来的时间。';
      isValid = false;
    }
  }

  // 如果是学生模式，且是修改密码，则密码不能为空
  if (props.viewMode === 'student' && isEditing.value && form.password && form.password.trim() === '') {
    validationErrors.value.password = '密码不能为空。';
    isValid = false;
  }


  if (!isValid) {
    // 显示第一个错误通知
    const firstError = Object.values(validationErrors.value)[0];
    showNotification(firstError, 'error');
  }
  return isValid;
};

// 进入编辑模式
const startEditing = () => {
  isEditing.value = true;
  validationErrors.value = {}; // 清空错误信息
};

// 取消编辑，恢复原始数据
const cancelEditing = () => {
  editableStudent.value = { ...props.student, password: '' };
  validationErrors.value = {};
  isEditing.value = false;
};

// 保存逻辑，根据模式决定做什么
const handleSave = () => {
  if (validateForm()) {
    // 父组件会监听 'create-student' 或 'update-student' 事件
    // 并调用对应的API
    if (isCreatingNew.value) {
      emit('create-student', editableStudent.value);
    } else {
      // 学生模式下，只允许更新特定字段
      if (props.viewMode === 'student') {
        const profileData = {
          password: editableStudent.value.password,
          phoneNumber: editableStudent.value.phoneNumber,
          email: editableStudent.value.email
        };
        emit('update-student', { id: editableStudent.value.id, ...profileData });
      } else {
        // 管理员模式下，更新所有可编辑字段
        emit('update-student', editableStudent.value);
      }
    }
    // 无论哪种模式，保存后都退出编辑状态
    // 注意：这里的isEditing=false应该由父组件在API调用成功后设置，
    // 以确保数据真正保存成功才退出编辑状态。
    // 但是为了UI的即时反馈，这里先设置，如果API失败，父组件会重新加载数据
    // 或者取消editing状态。这里保持原逻辑，父组件负责重置。
  }
};

// 删除逻辑（仅管理员可用）
const handleDelete = () => {
  if (!isCreatingNew.value) emit('delete-student', props.student.id);
};
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
  margin-top: var(--spacing-xs);
}
.header-actions {
  display: flex;
  gap: var(--spacing-md);
  flex-shrink: 0; /* 防止按钮被压缩 */
}
.action-btn {
  border: none;
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--border-radius);
  cursor: pointer;
  font-weight: 500;
  transition: all var(--transition-speed) ease;
  white-space: nowrap; /* 防止按钮文字换行 */
}
.action-btn.edit-btn { background-color: var(--color-primary-light); color: var(--color-primary); }
.action-btn.delete-btn { background-color: #fdf2f2; color: var(--color-danger); }
.action-btn.save-btn { background-color: var(--color-success); color: white; }
.action-btn.cancel-btn { background-color: #f1f3f5; color: var(--color-text-secondary); }
.action-btn:hover { opacity: 0.85; }
.action-btn:disabled {
  background-color: #e9ecef;
  color: #adb5bd;
  cursor: not-allowed;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--spacing-xl);
}
.info-card {
  background-color: var(--color-surface);
  padding: var(--spacing-lg) var(--spacing-xl);
  border-radius: var(--border-radius);
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
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--spacing-lg) var(--spacing-xl);
}
.info-item {
  display: grid;
  grid-template-columns: 100px 1fr;
  align-items: center;
  gap: var(--spacing-md);
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
  padding: 11px 0;
  min-height: 1.6em; /* 保证空数据时也有高度 */
  word-break: break-all; /* 防止长文本溢出 */
}
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
.editable-input:disabled {
  background-color: #f8f9fa;
  cursor: not-allowed;
  color: var(--color-text-secondary);
}
.input-error {
  border-color: var(--color-danger);
  box-shadow: 0 0 0 2px rgba(220, 53, 69, 0.2);
}
select.editable-input {
  /* 【关键修改】增加浏览器前缀，提升兼容性 */
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16'%3e%3cpath fill='none' stroke='%23343a40' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M2 5l6 6 6-6'/%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 0.75rem center;
  background-size: 16px 12px;
  padding-right: 2.5rem;
}
</style>