<template>
  <div class="page-container">
    <header class="page-header">
      <h1>班级管理</h1>
      <button @click="openAddModal" class="add-btn">新增班级</button>
    </header>

    <div class="content-card">
      <table class="data-table">
        <thead>
        <tr>
          <th>ID</th>
          <th>班级名称</th>
          <th>所属专业</th>
          <th>操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="isLoading">
          <td colspan="4" style="text-align: center; padding: 20px;">正在加载数据...</td>
        </tr>
        <tr v-for="cls in classes" :key="cls.id">
          <td>{{ cls.id }}</td>
          <td>{{ cls.name }}</td>
          <td>{{ cls.majorName }}</td>
          <td>
            <button @click="openEditModal(cls)" class="action-btn edit">编辑</button>
            <button @click="handleDeleteClass(cls.id)" class="action-btn delete">删除</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showEditModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h2>{{ isEditing ? '编辑班级' : '新增班级' }}</h2>
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label for="className">班级名称</label>
            <input v-model="editableClass.name" id="className" required>
          </div>
          <div class="form-group">
            <label for="major">所属专业</label>
            <select v-model="editableClass.majorId" id="major" required>
              <option disabled value="">请选择专业</option>
              <option v-for="major in majors" :key="major.id" :value="major.id">
                {{ major.name }}
              </option>
            </select>
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
import { classService, majorService } from '@/services/apiService';
import { showNotification } from '@/services/notificationStore';

const classes = ref([]);
const majors = ref([]);
const isLoading = ref(true);
const isSubmitting = ref(false);
const showEditModal = ref(false);
const isEditing = ref(false);
const editableClass = ref({ id: null, name: '', majorId: '' });

const fetchAllData = async () => {
  isLoading.value = true;
  try {
    const [classesRes, majorsRes] = await Promise.all([
      classService.getAll(),
      majorService.getAll()
    ]);
    classes.value = classesRes;
    majors.value = majorsRes;
  } catch (error) {
    showNotification(error.message || '获取数据失败', 'error');
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchAllData);

const resetForm = () => {
  editableClass.value = { id: null, name: '', majorId: '' };
};

const openAddModal = () => {
  resetForm();
  isEditing.value = false;
  showEditModal.value = true;
};

const openEditModal = (cls) => {
  editableClass.value = { ...cls };
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
    const payload = { ...editableClass.value };
    if (isEditing.value) {
      await classService.update(payload.id, payload);
      showNotification('班级更新成功！', 'success');
    } else {
      await classService.create(payload);
      showNotification('班级创建成功！', 'success');
    }
    closeModal();
    await fetchAllData();
  } catch (error) {
    showNotification(error.message || '操作失败', 'error');
  } finally {
    isSubmitting.value = false;
  }
};

const handleDeleteClass = async (id) => {
  if (!confirm('确定要删除这个班级吗？请先确保没有学生属于这个班级。')) return;
  try {
    await classService.delete(id);
    showNotification('班级删除成功！', 'success');
    await fetchAllData();
  } catch (error) {
    showNotification(error.message || '删除失败', 'error');
  }
};
</script>