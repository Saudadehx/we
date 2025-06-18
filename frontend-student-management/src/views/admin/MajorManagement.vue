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

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { majorService } from '@/services/apiService';
import { showNotification } from '@/services/notificationStore';

const majors = ref([]);
const isLoading = ref(true);
const isSubmitting = ref(false);
const showEditModal = ref(false);
const isEditing = ref(false);
const editableMajor = ref({ id: null, name: '', description: '' });

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

const closeModal = () => {
  showEditModal.value = false;
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