<template>
  <div class="page-container">
    <header class="page-header">
      <h1>专业管理</h1>
      <button @click="openAddModal" class="add-btn">新增专业</button>
    </header>

    <div class="content-card">
      <table class="data-table">
        <thead>
        <tr>
          <th>ID</th>
          <th>专业名称</th>
          <th>描述</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="isLoading">
          <td colspan="4" style="text-align: center; padding: 20px;">正在加载数据...</td>
        </tr>
        <tr v-for="major in majors" :key="major.id">
          <td>{{ major.id }}</td>
          <td>{{ major.name }}</td>
          <td>{{ major.description }}</td>
          <td>
            <button @click="openEditModal(major)" class="action-btn edit">编辑</button>
            <button @click="openAssignModal(major)" class="action-btn assign">分配课程</button>
            <button @click="handleDeleteMajor(major.id)" class="action-btn delete">删除</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showEditModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h2>{{ isEditing ? '编辑专业' : '新增专业' }}</h2>
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label for="majorName">专业名称</label>
            <input v-model="editableMajor.name" id="majorName" required>
          </div>
          <div class="form-group">
            <label for="description">描述</label>
            <textarea v-model="editableMajor.description" id="description" rows="4"></textarea>
          </div>
          <div class="modal-actions">
            <button type="button" @click="closeModal">取消</button>
            <button type="submit" :disabled="isSubmitting">{{ isSubmitting ? '处理中...' : '提交' }}</button>
          </div>
        </form>
      </div>
    </div>

    <div v-if="showAssignModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h2>为【{{ targetMajor.name }}】分配必修课</h2>
        <p>此操作将为该专业下指定年级和学期的所有学生，自动选上对应的必修课程。</p>
        <form @submit.prevent="handleAssignSubmit">
          <div class="form-group">
            <label for="academicYear">选择学年</label>
            <select v-model="assignParams.academicYear" id="academicYear" required>
              <option disabled value="">请选择</option>
              <option v-for="n in 4" :key="n" :value="n">第 {{ n }} 学年</option>
            </select>
          </div>
          <div class="form-group">
            <label for="semester">选择学期</label>
            <select v-model="assignParams.semester" id="semester" required>
              <option disabled value="">请选择</option>
              <option value="1">上学期</option>
              <option value="2">下学期</option>
            </select>
          </div>
          <div class="modal-actions">
            <button type="button" @click="closeModal">取消</button>
            <button type="submit" :disabled="isSubmitting">{{ isSubmitting ? '分配中...' : '确认分配' }}</button>
          </div>
        </form>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { majorService, enrollmentService } from '@/services/apiService'; // 引入 enrollmentService
import { showNotification } from '@/services/notificationStore';

const majors = ref([]);
const isLoading = ref(true);
const isSubmitting = ref(false);
const showEditModal = ref(false);
const showAssignModal = ref(false); // 新增
const isEditing = ref(false);
const editableMajor = ref({ id: null, name: '', description: '' });
const targetMajor = ref(null); // 新增
const assignParams = ref({ academicYear: '', semester: '' }); // 新增

const fetchMajors = async () => {
  isLoading.value = true;
  try {
    majors.value = await majorService.getAll();
  } catch (error) {
    showNotification(error.message || '获取专业列表失败', 'error');
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchMajors);

const resetForm = () => {
  editableMajor.value = { id: null, name: '', description: '' };
  assignParams.value = { academicYear: '', semester: '' };
  targetMajor.value = null;
};

const openAddModal = () => {
  resetForm();
  isEditing.value = false;
  showEditModal.value = true;
};

const openEditModal = (major) => {
  editableMajor.value = { ...major };
  isEditing.value = true;
  showEditModal.value = true;
};

const openAssignModal = (major) => {
  resetForm();
  targetMajor.value = major;
  showAssignModal.value = true;
};

const closeModal = () => {
  showEditModal.value = false;
  showAssignModal.value = false;
  resetForm();
};

const handleSubmit = async () => {
  isSubmitting.value = true;
  try {
    const payload = { ...editableMajor.value };
    if (isEditing.value) {
      await majorService.update(payload.id, payload);
      showNotification('专业更新成功！', 'success');
    } else {
      await majorService.create(payload);
      showNotification('专业创建成功！', 'success');
    }
    closeModal();
    await fetchMajors();
  } catch (error) {
    showNotification(error.message || '操作失败', 'error');
  } finally {
    isSubmitting.value = false;
  }
};

const handleAssignSubmit = async () => {
  isSubmitting.value = true;
  try {
    const response = await enrollmentService.assignCompulsoryCourses(
        targetMajor.value.id,
        assignParams.value.academicYear,
        assignParams.value.semester
    );
    showNotification(response.message || '课程分配成功！', 'success');
    closeModal();
  } catch (error) {
    showNotification(error.message || '分配失败', 'error');
  } finally {
    isSubmitting.value = false;
  }
};

const handleDeleteMajor = async (id) => {
  if (!confirm('确定要删除这个专业吗？相关的学生和课程信息可能会受影响。')) return;
  try {
    await majorService.delete(id);
    showNotification('专业删除成功！', 'success');
    await fetchMajors();
  } catch (error) {
    showNotification(error.message || '删除失败', 'error');
  }
};
</script>

<style scoped>
@import '@/assets/styles/common-page.css';
@import '@/assets/styles/common-modal.css';

.action-btn.assign {
  color: var(--color-success);
  background-color: #eaf6ec;
}
textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
  font-family: inherit;
  resize: vertical;
}
</style>