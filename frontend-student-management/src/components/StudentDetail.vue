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
          <button v-if="!isEditing" class="action-btn edit-btn" @click="startEditing">编辑</button>
          <button v-if="!isEditing" class="action-btn delete-btn" @click="handleDelete">删除</button>
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
            <span v-if="!isEditing">{{ student.name }}</span>
            <input v-else v-model="editableStudent.name" class="editable-input" :disabled="viewMode === 'student'"/>
          </div>
          <div class="info-item">
            <label>性别</label>
            <span v-if="!isEditing">{{ student.gender }}</span>
            <select v-else v-model="editableStudent.gender" class="editable-input" :disabled="viewMode === 'student'">
              <option>男</option><option>女</option><option>其他</option>
            </select>
          </div>
          <div class="info-item">
            <label>出生日期</label>
            <span v-if="!isEditing">{{ student.dateOfBirth }}</span>
            <input v-else type="date" v-model="editableStudent.dateOfBirth" class="editable-input" :disabled="viewMode === 'student'"/>
          </div>
          <div class="info-item">
            <label>民族</label>
            <span v-if="!isEditing">{{ student.ethnicity }}</span>
            <input v-else v-model="editableStudent.ethnicity" class="editable-input"/>
          </div>
          <div class="info-item">
            <label>籍贯</label>
            <span v-if="!isEditing">{{ student.nativePlace }}</span>
            <input v-else v-model="editableStudent.nativePlace" class="editable-input"/>
          </div>
          <div class="info-item">
            <label>政治面貌</label>
            <span v-if="!isEditing">{{ student.politicalStatus }}</span>
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
            <input v-else v-model="editableStudent.phoneNumber" class="editable-input"/>
          </div>
          <div class="info-item">
            <label>电子邮箱</label>
            <span v-if="!isEditing">{{ student.email }}</span>
            <input v-else type="email" v-model="editableStudent.email" class="editable-input"/>
          </div>
        </div>
      </div>

      <div class="info-card">
        <h4>学籍信息</h4>
        <div class="form-grid">
          <div class="info-item">
            <label>学号</label>
            <span>{{ student.studentId }}</span>
          </div>
          <div class="info-item">
            <label>入学日期</label>
            <span v-if="!isEditing">{{ student.enrollmentDate }}</span>
            <input v-else type="date" v-model="editableStudent.enrollmentDate" class="editable-input" :disabled="viewMode === 'student'"/>
          </div>
          <div class="info-item">
            <label>学院</label>
            <span v-if="!isEditing">{{ student.college }}</span>
            <input v-else v-model="editableStudent.college" class="editable-input" :disabled="viewMode === 'student'"/>
          </div>
          <div class="info-item">
            <label>班级</label>
            <span v-if="!isEditing">{{ student.className }}</span>
            <input v-else v-model="editableStudent.className" class="editable-input" :disabled="viewMode === 'student'"/>
          </div>
          <div class="info-item">
            <label>专业</label>
            <span v-if="!isEditing">{{ student.majorName || '未分配' }}</span>
            <select v-else v-model="editableStudent.majorId" class="editable-input" :disabled="viewMode === 'student'">
              <option :value="null">未分配</option>
              <option v-for="major in majors" :key="major.id" :value="major.id">{{ major.name }}</option>
            </select>
          </div>
          <div class="info-item">
            <label>当前学年</label>
            <span v-if="!isEditing">{{ student.academicYear ? `第 ${student.academicYear} 学年` : '未设置' }}</span>
            <select v-else v-model="editableStudent.academicYear" class="editable-input" :disabled="viewMode === 'student'">
              <option :value="null">未设置</option>
              <option v-for="n in 4" :key="n" :value="n">第 {{ n }} 学年</option>
            </select>
          </div>
          <div class="info-item">
            <label>当前学期</label>
            <span v-if="!isEditing">{{ formatSemester(student.semester) }}</span>
            <select v-else v-model="editableStudent.semester" class="editable-input" :disabled="viewMode === 'student'">
              <option :value="null">未设置</option>
              <option value="1">上学期</option>
              <option value="2">下学期</option>
            </select>
          </div>
          <div class="info-item">
            <label>学籍状态</label>
            <span v-if="!isEditing">{{ student.studentStatus }}</span>
            <select v-else v-model="editableStudent.studentStatus" class="editable-input" :disabled="viewMode === 'student'">
              <option>在读</option><option>休学</option><option>毕业</option><option>退学</option>
            </select>
          </div>
        </div>
      </div>

      <div class="info-card" v-if="isEditing">
        <h4>账户信息</h4>
        <div class="form-grid">
          <div class="info-item">
            <label>重置密码</label>
            <input type="password" v-model="editableStudent.password" class="editable-input" :placeholder="isCreatingNew ? '请输入初始密码' : '不修改请留空'"/>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { showNotification } from '@/services/notificationStore.js';
import { majorService } from '@/services/apiService';

const props = defineProps({
  student: { type: Object, required: true },
  viewMode: { type: String, default: 'admin' },
  isSaving: { type: Boolean, default: false }
});

const emit = defineEmits(['create-student', 'update-student', 'delete-student']);

const majors = ref([]);
const isEditing = ref(false);
const editableStudent = ref({});
const isCreatingNew = computed(() => !props.student.id);

onMounted(async () => {
  if (props.viewMode === 'admin') {
    try {
      majors.value = await majorService.getAll();
    } catch (error) {
      showNotification('加载专业列表失败', 'error');
    }
  }
});

watch(() => props.student, (newStudent) => {
  if (newStudent) {
    editableStudent.value = { ...newStudent, password: '' };
    isEditing.value = isCreatingNew.value;
  }
}, { immediate: true, deep: true });

const formatSemester = (semester) => {
  if (semester === 1) return '上学期';
  if (semester === 2) return '下学期';
  return '未设置';
};

const startEditing = () => { isEditing.value = true; };

const cancelEditing = () => {
  editableStudent.value = { ...props.student, password: '' };
  isEditing.value = false;
};

const handleSave = () => {
  if (isCreatingNew.value) {
    emit('create-student', editableStudent.value);
  } else {
    emit('update-student', editableStudent.value);
  }
};

const handleDelete = () => {
  if (!isCreatingNew.value) {
    emit('delete-student', props.student.id);
  }
};
</script>

<style scoped>
/* 样式与之前相同，此处省略以保持简洁 */
.detail-container { padding: var(--spacing-xl); overflow-y: auto; height: 100%; box-sizing: border-box; }
.header { display: flex; justify-content: space-between; align-items: flex-start; padding-bottom: var(--spacing-lg); border-bottom: 1px solid var(--color-border); margin-bottom: var(--spacing-xl); }
.header-info h3 { margin: 0; font-size: 1.8em; color: var(--color-text-primary); }
.header-subtitle { margin: 0; color: var(--color-text-secondary); font-size: 1em; margin-top: var(--spacing-xs); }
.header-actions { display: flex; gap: var(--spacing-md); flex-shrink: 0; }
.action-btn { border: none; padding: var(--spacing-sm) var(--spacing-md); border-radius: var(--border-radius); cursor: pointer; font-weight: 500; transition: all var(--transition-speed) ease; white-space: nowrap; }
.action-btn.edit-btn { background-color: var(--color-primary-light); color: var(--color-primary); }
.action-btn.delete-btn { background-color: #fdf2f2; color: var(--color-danger); }
.action-btn.save-btn { background-color: var(--color-success); color: white; }
.action-btn.cancel-btn { background-color: #f1f3f5; color: var(--color-text-secondary); }
.action-btn:hover { opacity: 0.85; }
.action-btn:disabled { background-color: #e9ecef; color: #adb5bd; cursor: not-allowed; }
.content-grid { display: grid; grid-template-columns: 1fr; gap: var(--spacing-xl); }
.info-card { background-color: var(--color-surface); padding: var(--spacing-lg) var(--spacing-xl); border-radius: var(--border-radius); box-shadow: var(--box-shadow); }
.info-card h4 { margin-top: 0; margin-bottom: var(--spacing-lg); font-size: 1.2em; color: var(--color-text-primary); border-bottom: 1px solid var(--color-border); padding-bottom: var(--spacing-md); }
.form-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: var(--spacing-lg) var(--spacing-xl); }
.info-item { display: grid; grid-template-columns: 100px 1fr; align-items: center; gap: var(--spacing-md); }
.info-item label { color: var(--color-text-secondary); font-weight: 500; text-align: right; font-size: 0.9em; }
.info-item span { font-weight: 500; color: var(--color-text-primary); padding: 11px 0; min-height: 1.6em; word-break: break-all; }
.editable-input { font-family: inherit; font-size: 1em; padding: 10px 14px; border: 1px solid var(--color-border); border-radius: var(--border-radius); background-color: var(--color-surface); outline: none; transition: all var(--transition-speed) ease; width: 100%; box-sizing: border-box; }
.editable-input:focus { border-color: var(--color-primary); box-shadow: 0 0 0 3px var(--color-primary-light); }
.editable-input:disabled { background-color: #f8f9fa; cursor: not-allowed; color: var(--color-text-secondary); }
select.editable-input { -webkit-appearance: none; -moz-appearance: none; appearance: none; background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 16 16'%3e%3cpath fill='none' stroke='%23343a40' stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M2 5l6 6 6-6'/%3e%3c/svg%3e"); background-repeat: no-repeat; background-position: right 0.75rem center; background-size: 16px 12px; padding-right: 2.5rem; }
</style>