<template>
  <div class="detail-container" v-if="student">
    <div class="page-header">
      <div class="header-content">
        <div class="header-info">
          <h1 class="page-title">
            {{ pageTitle }}
          </h1>
          <p class="page-subtitle">
            {{ pageSubtitle }}
          </p>

          <div class="status-badges" v-if="!isCreatingNew">
            <span class="status-badge" :class="statusClass">
              {{ student.studentStatus }}
            </span>
            <span class="info-badge" v-if="student.academicYear">
              第{{ student.academicYear }}学年
            </span>
            <span class="info-badge" v-if="student.semester">
              {{ formattedSemester }}
            </span>
          </div>
        </div>
      </div>

      <div class="header-actions">
        <template v-if="viewMode === 'admin'">
          <button v-if="!isEditing && !isCreatingNew" class="action-btn edit-btn" @click="startEditing">
            <svg class="btn-icon" viewBox="0 0 20 20" fill="currentColor">
              <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z"/>
            </svg>
            编辑档案
          </button>
          <button v-if="!isEditing && !isCreatingNew" class="action-btn delete-btn" @click="handleDelete">
            <svg class="btn-icon" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M9 2a1 1 0 000 2h2a1 1 0 100-2H9zM4 5a2 2 0 012-2h8a2 2 0 012 2v10a2 2 0 01-2 2H6a2 2 0 01-2-2V5zm3 4a1 1 0 112 0v4a1 1 0 11-2 0V9zm4 0a1 1 0 112 0v4a1 1 0 11-2 0V9z" clip-rule="evenodd"/>
            </svg>
            删除档案
          </button>
          <button v-if="isEditing" class="action-btn save-btn" @click="handleSave" :disabled="isSaving">
            <div v-if="isSaving" class="loading-spinner"></div>
            <svg v-else class="btn-icon" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
            </svg>
            <span>{{ saveButtonText }}</span>
          </button>
          <button v-if="isEditing && !isCreatingNew" class="action-btn cancel-btn" @click="cancelEditing">
            <svg class="btn-icon" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/>
            </svg>
            取消编辑
          </button>
        </template>

        <template v-else-if="viewMode === 'student'">
          <button v-if="!isEditing" class="action-btn edit-btn" @click="startEditing">
            <svg class="btn-icon" viewBox="0 0 20 20" fill="currentColor">
              <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z"/>
            </svg>
            修改信息
          </button>
          <button v-if="isEditing" class="action-btn save-btn" @click="handleSave" :disabled="isSaving">
            <div v-if="isSaving" class="loading-spinner"></div>
            <svg v-else class="btn-icon" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
            </svg>
            <span>{{ isSaving ? '保存中...' : '保存更改' }}</span>
          </button>
          <button v-if="isEditing" class="action-btn cancel-btn" @click="cancelEditing">
            <svg class="btn-icon" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/>
            </svg>
            取消
          </button>
        </template>
      </div>
    </div>

    <div class="content-wrapper">
      <div class="content-grid">
        <div class="info-card">
          <div class="card-header">
            <div class="card-title">
              <svg class="card-icon" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"/>
              </svg>
              <h4>基本信息</h4>
            </div>
          </div>
          <div class="card-content">
            <div class="form-grid">
              <div class="info-item">
                <label class="item-label">姓名</label>
                <div class="item-content">
                  <span v-if="!isEditing" class="item-value">{{ student.name }}</span>
                  <input v-else v-model="editableStudent.name" class="form-input" :class="{'is-invalid': errors.name}" :disabled="viewMode === 'student'" placeholder="请输入姓名"/>
                  <span v-if="errors.name" class="error-message">{{ errors.name }}</span>
                </div>
              </div>

              <div class="info-item">
                <label class="item-label">性别</label>
                <div class="item-content">
                  <span v-if="!isEditing" class="item-value">{{ student.gender }}</span>
                  <select v-else v-model="editableStudent.gender" class="form-select" :class="{'is-invalid': errors.gender}" :disabled="viewMode === 'student'">
                    <option value="男">男</option>
                    <option value="女">女</option>
                    <option value="其他">其他</option>
                  </select>
                  <span v-if="errors.gender" class="error-message">{{ errors.gender }}</span>
                </div>
              </div>

              <div class="info-item">
                <label class="item-label">出生日期</label>
                <div class="item-content">
                  <span v-if="!isEditing" class="item-value">{{ formattedBirthDate }}</span>
                  <input v-else type="date" v-model="editableStudent.dateOfBirth" class="form-input" :class="{'is-invalid': errors.dateOfBirth}" :disabled="viewMode === 'student'" :min="dateValidation.birthDate.min" :max="dateValidation.birthDate.max"/>
                  <span v-if="errors.dateOfBirth" class="error-message">{{ errors.dateOfBirth }}</span>
                </div>
              </div>

              <div class="info-item">
                <label class="item-label">民族</label>
                <div class="item-content">
                  <span v-if="!isEditing" class="item-value">{{ student.ethnicity || '未填写' }}</span>
                  <input v-else v-model="editableStudent.ethnicity" class="form-input" placeholder="请输入民族"/>
                </div>
              </div>

              <div class="info-item">
                <label class="item-label">籍贯</label>
                <div class="item-content">
                  <span v-if="!isEditing" class="item-value">{{ student.nativePlace || '未填写' }}</span>
                  <input v-else v-model="editableStudent.nativePlace" class="form-input" placeholder="请输入籍贯"/>
                </div>
              </div>

              <div class="info-item">
                <label class="item-label">政治面貌</label>
                <div class="item-content">
                  <span v-if="!isEditing" class="item-value">{{ student.politicalStatus || '未填写' }}</span>
                  <select v-else v-model="editableStudent.politicalStatus" class="form-select">
                    <option value="群众">群众</option>
                    <option value="共青团员">共青团员</option>
                    <option value="中共预备党员">中共预备党员</option>
                    <option value="中共党员">中共党员</option>
                    <option value="其他">其他</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="info-card">
          <div class="card-header">
            <div class="card-title">
              <svg class="card-icon" viewBox="0 0 20 20" fill="currentColor">
                <path d="M2 3a1 1 0 011-1h2.153a1 1 0 01.986.836l.74 4.435a1 1 0 01-.54 1.06l-1.548.773a11.037 11.037 0 006.105 6.105l.774-1.548a1 1 0 011.059-.54l4.435.74a1 1 0 01.836.986V17a1 1 0 01-1 1h-2C7.82 18 2 12.18 2 5V3z"/>
              </svg>
              <h4>联系方式</h4>
            </div>
          </div>
          <div class="card-content">
            <div class="form-grid">
              <div class="info-item">
                <label class="item-label">手机号码</label>
                <div class="item-content">
                  <span v-if="!isEditing" class="item-value">{{ student.phoneNumber || '未填写' }}</span>
                  <input v-else v-model="editableStudent.phoneNumber" class="form-input" placeholder="请输入手机号码" type="tel"/>
                </div>
              </div>

              <div class="info-item">
                <label class="item-label">电子邮箱</label>
                <div class="item-content">
                  <span v-if="!isEditing" class="item-value">{{ student.email || '未填写' }}</span>
                  <input v-else type="email" v-model="editableStudent.email" class="form-input" placeholder="请输入电子邮箱"/>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="info-card">
          <div class="card-header">
            <div class="card-title">
              <svg class="card-icon" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M6 6V5a3 3 0 013-3h2a3 3 0 013 3v1h2a2 2 0 012 2v3.57A22.952 22.952 0 0110 13a22.95 22.95 0 01-8-1.43V8a2 2 0 012-2h2zm2-1a1 1 0 011-1h2a1 1 0 011 1v1H8V5zm1 5a1 1 0 011-1h.01a1 1 0 110 2H10a1 1 0 01-1-1z" clip-rule="evenodd"/>
                <path d="M2 13.692V16a2 2 0 002 2h12a2 2 0 002-2v-2.308A24.974 24.974 0 0110 15c-2.796 0-5.487-.46-8-1.308z"/>
              </svg>
              <h4>学籍信息</h4>
            </div>
          </div>
          <div class="card-content">
            <div class="form-grid">
              <div class="info-item">
                <label class="item-label">学号</label>
                <div class="item-content">
                  <span v-if="!isEditing" class="item-value primary-text">{{ student.studentId }}</span>
                  <input v-else v-model="editableStudent.studentId" class="form-input" :class="{'is-invalid': errors.studentId}" :disabled="!isCreatingNew" placeholder="请输入学号" />
                  <span v-if="errors.studentId" class="error-message">{{ errors.studentId }}</span>
                </div>
              </div>
              <div class="info-item">
                <label class="item-label">入学日期</label>
                <div class="item-content">
                  <span v-if="!isEditing" class="item-value">{{ formattedEnrollmentDate }}</span>
                  <input v-else type="date" v-model="editableStudent.enrollmentDate" class="form-input" :disabled="viewMode === 'student'" :min="dateValidation.enrollmentDate.min" :max="dateValidation.enrollmentDate.max"/>
                </div>
              </div>

              <div class="info-item">
                <label class="item-label">学院</label>
                <div class="item-content">
                  <span v-if="!isEditing" class="item-value">{{ student.college || '未填写' }}</span>
                  <input v-else v-model="editableStudent.college" class="form-input" :disabled="viewMode === 'student'" placeholder="请输入学院"/>
                </div>
              </div>

              <div class="info-item">
                <label class="item-label">班级</label>
                <div class="item-content">
                  <span v-if="!isEditing" class="item-value">{{ student.className || '未分配' }}</span>
                  <template v-else>
                    <input v-if="viewMode === 'student'"
                           :value="student.className || '未分配'"
                           class="form-input"
                           disabled
                           readonly />
                    <template v-else-if="viewMode === 'admin'">
                      <select v-model="editableStudent.classId" class="form-select" :disabled="classSelectDisabled">
                        <option :value="null">请先选择专业</option>
                        <option v-for="cls in filteredClasses" :key="cls.id" :value="cls.id">
                          {{ cls.name }}
                        </option>
                      </select>
                      <p v-if="showClassHint" class="field-hint">请先在上方选择一个专业，才能分配班级。</p>
                    </template>
                  </template>
                </div>
              </div>

              <div class="info-item">
                <label class="item-label">专业</label>
                <div class="item-content">
                  <span v-if="!isEditing" class="item-value">{{ student.majorName || '未分配' }}</span>
                  <template v-else>
                    <input v-if="viewMode === 'student'" :value="editableStudent.majorName || '未分配'" class="form-input" disabled />
                    <select v-else-if="viewMode === 'admin'" v-model="editableStudent.majorId" class="form-select">
                      <option :value="null">未分配</option>
                      <option v-for="major in majors" :key="major.id" :value="major.id">
                        {{ major.name }}
                      </option>
                    </select>
                  </template>
                </div>
              </div>

              <div class="info-item">
                <label class="item-label">当前学年</label>
                <div class="item-content">
                  <span v-if="!isEditing" class="item-value">{{ formattedAcademicYear }}</span>
                  <select v-else v-model.number="editableStudent.academicYear" class="form-select" :disabled="viewMode === 'student'">
                    <option :value="null">未设置</option>
                    <option v-for="n in 4" :key="n" :value="n">第 {{ n }} 学年</option>
                  </select>
                </div>
              </div>

              <div class="info-item">
                <label class="item-label">当前学期</label>
                <div class="item-content">
                  <span v-if="!isEditing" class="item-value">{{ formattedCurrentSemester }}</span>
                  <select v-else v-model.number="editableStudent.semester" class="form-select" :disabled="viewMode === 'student'">
                    <option :value="null">未设置</option>
                    <option value="1">上学期</option>
                    <option value="2">下学期</option>
                  </select>
                </div>
              </div>

              <div class="info-item">
                <label class="item-label">学籍状态</label>
                <div class="item-content">
                  <span v-if="!isEditing" class="item-value">{{ student.studentStatus }}</span>
                  <select v-else v-model="editableStudent.studentStatus" class="form-select" :disabled="viewMode === 'student'">
                    <option value="在读">在读</option>
                    <option value="休学">休学</option>
                    <option value="毕业">毕业</option>
                    <option value="退学">退学</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="info-card" v-if="isEditing">
          <div class="card-header">
            <div class="card-title">
              <svg class="card-icon" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd"/>
              </svg>
              <h4>{{ viewMode === 'admin' ? '账户安全' : '修改密码' }}</h4>
            </div>
          </div>
          <div class="card-content">
            <div class="form-grid">
              <div class="info-item full-width">
                <label class="item-label">
                  {{ passwordLabel }}
                </label>
                <div class="item-content">
                  <input type="password" v-model="editableStudent.password" class="form-input" :class="{'is-invalid': errors.password}" :placeholder="passwordPlaceholder"/>
                  <span v-if="errors.password" class="error-message">{{ errors.password }}</span>
                  <p v-else class="field-hint">
                    {{ passwordHint }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, shallowRef } from 'vue';
import { showNotification } from '@/services/notificationStore.js';
import { classService, majorService } from '@/services/apiService';

const props = defineProps({
  student: { type: Object, required: true },
  viewMode: { type: String, default: 'admin' },
  isSaving: { type: Boolean, default: false }
});

const emit = defineEmits(['create-student', 'update-student', 'delete-student']);

// Use shallowRef for data that doesn't need deep reactivity
const allClasses = shallowRef([]);
const majors = shallowRef([]);
const isEditing = ref(false);
const editableStudent = ref({});
const errors = ref({});

// Cached computed values
const isCreatingNew = computed(() => !props.student.id);

// Memoized computed properties for formatting
const pageTitle = computed(() =>
    isCreatingNew.value ? '新增学生档案' :
        (props.viewMode === 'admin' ? editableStudent.value.name : '我的个人档案')
);

const pageSubtitle = computed(() =>
    isCreatingNew.value ? '请完善以下学生信息' :
        (props.viewMode === 'admin' ? `学号：${editableStudent.value.studentId}` :
            `${props.student.name} (${props.student.studentId})`)
);

const statusClass = computed(() => {
  const statusMap = {
    '休学': 'status-warning',
    '毕业': 'status-success',
    '退学': 'status-danger'
  };
  return statusMap[props.student.studentStatus] || '';
});

const formattedSemester = computed(() => {
  return props.student.semester === 1 ? '上学期' :
      props.student.semester === 2 ? '下学期' : '未设置';
});

const formattedBirthDate = computed(() => {
  return props.student.dateOfBirth ?
      new Date(props.student.dateOfBirth).toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      }) : '未填写';
});

const formattedEnrollmentDate = computed(() => {
  return props.student.enrollmentDate ?
      new Date(props.student.enrollmentDate).toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      }) : '未填写';
});

const formattedAcademicYear = computed(() => {
  return props.student.academicYear ? `第 ${props.student.academicYear} 学年` : '未设置';
});

const formattedCurrentSemester = computed(() => {
  return props.student.semester === 1 ? '上学期' :
      props.student.semester === 2 ? '下学期' : '未设置';
});

const saveButtonText = computed(() =>
    props.isSaving ? '保存中...' :
        (isCreatingNew.value ? '创建档案' : '保存更改')
);

const passwordLabel = computed(() =>
    isCreatingNew.value ? '设置初始密码' :
        (props.viewMode === 'admin' ? '重置密码' : '新密码')
);

const passwordPlaceholder = computed(() =>
    isCreatingNew.value ? '请输入初始密码（必填）' : '不修改请留空'
);

const passwordHint = computed(() =>
    isCreatingNew.value ? '建议使用包含字母和数字的组合密码。' :
        (props.viewMode === 'admin' ? '输入新密码将重置该学生的登录密码。' : '输入新密码以修改当前密码。')
);

// Optimized class-related computed properties
const classSelectDisabled = computed(() =>
    !editableStudent.value.majorId || filteredClasses.value.length === 0
);

const showClassHint = computed(() => !editableStudent.value.majorId);

const filteredClasses = computed(() => {
  if (!editableStudent.value.majorId || !allClasses.value.length) {
    return [];
  }
  return allClasses.value.filter(cls => cls.majorId === editableStudent.value.majorId);
});

// Cached date validation to prevent recalculation
const dateValidation = computed(() => {
  const today = new Date().toISOString().split('T')[0];
  const currentYear = new Date().getFullYear();
  return {
    birthDate: { min: '1920-01-01', max: today },
    enrollmentDate: { min: '2000-01-01', max: `${currentYear + 5}-12-31` }
  };
});

// Optimized data loading
let dataLoaded = false;
onMounted(async () => {
  if (props.viewMode === 'admin' && !dataLoaded) {
    try {
      const [majorsRes, classesRes] = await Promise.all([
        majorService.getAll(),
        classService.getAll()
      ]);
      majors.value = majorsRes;
      allClasses.value = classesRes;
      dataLoaded = true;
    } catch (error) {
      showNotification('加载专业或班级列表失败', 'error');
    }
  }
});

// Optimized watcher with immediate flag optimization
watch(() => props.student, (newStudent) => {
  if (newStudent) {
    // Use Object.assign for better performance than JSON stringify/parse
    editableStudent.value = Object.assign({}, newStudent, { password: '' });
    isEditing.value = isCreatingNew.value;
    errors.value = {};
  }
}, { immediate: true });

// Optimized validation function
const validateForm = () => {
  const newErrors = {};
  const { name, studentId, gender, dateOfBirth, className, password } = editableStudent.value;

  if (!name) {
    newErrors.name = '姓名不能为空';
  } else if (name.length < 2 || name.length > 50) {
    newErrors.name = '姓名长度必须在2到50之间';
  }

  if (!studentId) {
    newErrors.studentId = '学号不能为空';
  } else if (studentId.length < 4 || studentId.length > 20) {
    newErrors.studentId = '学号长度必须在4到20之间';
  }

  if (!gender) newErrors.gender = '性别不能为空';
  if (!dateOfBirth) newErrors.dateOfBirth = '出生日期不能为空';
  if (!className) newErrors.className = '班级名称不能为空';

  if (isCreatingNew.value && !password) {
    newErrors.password = '创建学生时必须设置初始密码';
  }

  errors.value = newErrors;
  return Object.keys(newErrors).length === 0;
};

const startEditing = () => {
  isEditing.value = true;
};

const cancelEditing = () => {
  editableStudent.value = Object.assign({}, props.student, { password: '' });
  isEditing.value = false;
  errors.value = {};
};

const handleSave = async () => {
  errors.value = {};

  if (!validateForm()) {
    showNotification('部分字段填写不正确，请检查。', 'error');
    return;
  }

  if (props.viewMode === 'admin' && editableStudent.value.majorId) {
    const selectedMajor = majors.value.find(m => m.id === editableStudent.value.majorId);
    if (selectedMajor) editableStudent.value.majorName = selectedMajor.name;
  }

  try {
    const eventName = isCreatingNew.value ? 'create-student' : 'update-student';
    emit(eventName, editableStudent.value);
  } catch (error) {
    if (error.status === 400 && error.data) {
      errors.value = error.data;
      showNotification(error.message || '提交的数据有误', 'error');
    } else {
      showNotification(error.message || '操作失败', 'error');
    }
  }
};

const handleDelete = () => {
  if (!isCreatingNew.value) {
    emit('delete-student', props.student.id);
  }
};
</script>

<style scoped>
.detail-container {
  height: 100%;
  background: linear-gradient(135deg, var(--color-background) 0%, #f8fafc 50%, var(--color-primary-light) 100%);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.page-header {
  background: linear-gradient(135deg, rgba(var(--color-primary-rgb), 0.95), rgba(var(--color-primary-rgb), 0.85));
  color: white;
  padding: var(--spacing-xl);
  box-shadow: 0 4px 20px rgba(var(--color-primary-rgb), 0.3);
  position: relative;
}

.page-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1000 100" fill="rgba(255,255,255,0.05)"><polygon points="0,0 1000,0 0,100"/></svg>');
  pointer-events: none;
}

.header-content {
  position: relative;
  z-index: 1;
}

.header-info {
  flex: 1;
}

.page-title {
  font-size: 2.2em;
  font-weight: 700;
  margin: 0 0 var(--spacing-sm);
  letter-spacing: -0.5px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.page-subtitle {
  font-size: 1.1em;
  margin: 0 0 var(--spacing-md);
  opacity: 0.9;
  font-weight: 500;
}

.status-badges {
  display: flex;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

.status-badge, .info-badge {
  padding: 6px var(--spacing-md);
  border-radius: 20px;
  font-size: 0.85em;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  transition: all var(--transition-speed) ease;
}

.status-badge:hover, .info-badge:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
}

.status-warning {
  background-color: var(--color-warning) !important;
  color: white !important;
  border-color: rgba(255, 255, 255, 0.3) !important;
}
.status-success {
  background-color: var(--color-success) !important;
  color: white !important;
  border-color: rgba(255, 255, 255, 0.3) !important;
}
.status-danger {
  background-color: var(--color-danger) !important;
  color: white !important;
  border-color: rgba(255, 255, 255, 0.3) !important;
}

.header-actions {
  position: absolute;
  bottom: var(--spacing-lg);
  right: var(--spacing-xl);
  display: flex;
  gap: var(--spacing-sm);
  z-index: 1;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 10px var(--spacing-md);
  border: none;
  border-radius: var(--border-radius);
  font-weight: 600;
  font-size: 0.9em;
  cursor: pointer;
  transition: all var(--transition-speed) ease;
  white-space: nowrap;
  border: 1px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
}

.action-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.action-btn:hover::before {
  left: 100%;
}

.btn-icon {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}

.edit-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}
.delete-btn {
  background: rgba(220, 53, 69, 0.2);
  color: white;
}
.save-btn {
  background: var(--color-success);
  color: white;
  border-color: rgba(255, 255, 255, 0.3);
}
.cancel-btn {
  background: rgba(108, 117, 125, 0.2);
  color: white;
}

.action-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}
.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.content-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-xl);
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--spacing-xl);
  max-width: 1200px;
  margin: 0 auto;
}

.info-card {
  background: var(--color-surface);
  border-radius: calc(var(--border-radius) * 1.5);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.06), 0 0 0 1px rgba(var(--color-primary-rgb), 0.05);
  overflow: hidden;
  transition: all var(--transition-speed) ease;
  position: relative;
}

.info-card::before {
  content: '';
  display: block;
  height: 3px;
  background: linear-gradient(90deg, var(--color-primary), rgba(var(--color-primary-rgb), 0.6));
}

.info-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.12), 0 0 0 1px rgba(var(--color-primary-rgb), 0.1);
}

.card-header {
  padding: var(--spacing-lg) var(--spacing-xl);
  background: linear-gradient(135deg, rgba(var(--color-primary-rgb), 0.03), transparent);
  border-bottom: 1px solid rgba(var(--color-primary-rgb), 0.1);
}

.card-title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.card-icon {
  width: 20px;
  height: 20px;
  color: var(--color-primary);
  flex-shrink: 0;
}

.card-header h4 {
  font-size: 1.3em;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
}

.card-content {
  padding: var(--spacing-xl);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--spacing-lg);
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.item-label {
  font-weight: 600;
  color: var(--color-text-primary);
  font-size: 0.9em;
  margin-bottom: 2px;
}

.item-content {
  position: relative;
}

.item-value {
  padding: 12px 0;
  color: var(--color-text-primary);
  font-weight: 500;
  min-height: 1.5em;
  line-height: 1.5;
  border-bottom: 1px solid transparent;
  transition: all var(--transition-speed) ease;
}

.item-value.primary-text {
  color: var(--color-primary);
  font-weight: 700;
  font-size: 1.1em;
}

.form-input, .form-select {
  width: 100%;
  padding: 12px var(--spacing-md);
  border: 2px solid var(--color-border);
  border-radius: var(--border-radius);
  font-size: 1em;
  font-family: inherit;
  background: #fcfdff;
  transition: all var(--transition-speed) ease;
  box-sizing: border-box;
}

.form-input:focus, .form-select:focus {
  outline: none;
  border-color: var(--color-primary);
  background: white;
  box-shadow: 0 0 0 3px rgba(var(--color-primary-rgb), 0.1), 0 2px 8px rgba(var(--color-primary-rgb), 0.15);
  transform: translateY(-1px);
}

.form-input:disabled, .form-select:disabled {
  background: #f8f9fa;
  cursor: not-allowed;
  color: var(--color-text-secondary);
  border-color: #e9ecef;
}

.form-select {
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16'%3e%3cpath fill='none' stroke='%23343a40' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M2 5l6 6 6-6'/%3e%3c/svg%3e");
  background-repeat: no-repeat;
  background-position: right 12px center;
  background-size: 16px 12px;
  padding-right: 40px;
}

.field-hint {
  margin: var(--spacing-sm) 0 0;
  font-size: 0.85em;
  color: var(--color-text-secondary);
  line-height: 1.4;
}

.form-input.is-invalid,
.form-select.is-invalid {
  border-color: var(--color-danger);
  box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.15);
  animation: shake 0.6s cubic-bezier(.36,.07,.19,.97) both;
}

.error-message {
  color: var(--color-danger);
  font-size: 0.85em;
  font-weight: 500;
  margin-top: var(--spacing-sm);
  display: flex;
  align-items: center;
  gap: 4px;
}

.error-message::before {
  content: '⚠';
  font-size: 0.9em;
}

@keyframes shake {
  10%, 90% { transform: translate3d(-1px, 0, 0); }
  20%, 80% { transform: translate3d(2px, 0, 0); }
  30%, 50%, 70% { transform: translate3d(-4px, 0, 0); }
  40%, 60% { transform: translate3d(4px, 0, 0); }
}
</style>