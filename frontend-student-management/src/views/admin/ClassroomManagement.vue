<template>
  <div class="page-container">
    <header class="page-header">
      <h1>教室管理</h1>
      <button @click="openAddModal" class="add-btn">新增教室</button>
    </header>

    <div class="content-card">
      <div v-if="isLoading" class="loading-indicator">
        正在加载教室数据...
      </div>
      <table v-else class="data-table">
        <thead>
        <tr>
          <th>ID</th>
          <th>教室名称/编号</th>
          <th>教室类型</th>
          <th>容量</th>
          <th style="width: 150px;">操作</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="classrooms.length === 0">
          <td colspan="5" class="no-data-cell">暂无教室信息，请点击“新增教室”进行添加。</td>
        </tr>
        <tr v-for="room in classrooms" :key="room.id">
          <td>{{ room.id }}</td>
          <td>{{ room.name }}</td>
          <td>{{ room.type }}</td>
          <td>{{ room.capacity }} 人</td>
          <td>
            <button @click="openEditModal(room)" class="action-btn edit">编辑</button>
            <button @click="handleDelete(room.id)" class="action-btn delete">删除</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h2>{{ isEditing ? '编辑教室' : '新增教室' }}</h2>
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label for="classroomName">教室名称/编号 <span class="required">*</span></label>
            <input v-model.trim="editableClassroom.name" id="classroomName" placeholder="例如: 教A-101" required>
          </div>
          <div class="form-group">
            <label for="classroomType">教室类型 <span class="required">*</span></label>
            <input v-model.trim="editableClassroom.type" id="classroomType" placeholder="例如: 标准教室, 计算机实验室" required>
          </div>
          <div class="form-group">
            <label for="classroomCapacity">教室容量 <span class="required">*</span></label>
            <input v-model.number="editableClassroom.capacity" id="classroomCapacity" type="number" min="1" placeholder="请输入正整数" required>
          </div>

          <div class="modal-actions">
            <button type="button" class="cancel-btn" @click="closeModal">取消</button>
            <button type="submit" class="submit-btn" :disabled="isSubmitting">
              {{ isSubmitting ? '处理中...' : '确认提交' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { classroomService } from '@/services/apiService';
import { showNotification } from '@/services/notificationStore';

// 响应式状态
const classrooms = ref([]);
const isLoading = ref(true);
const isSubmitting = ref(false);
const showModal = ref(false);
const isEditing = ref(false);

// 用于模态框表单数据绑定的对象
const getNewEditableClassroom = () => ({
  id: null,
  name: '',
  type: '标准教室',
  capacity: 30
});
const editableClassroom = ref(getNewEditableClassroom());

// 获取所有教室数据
const fetchClassrooms = async () => {
  isLoading.value = true;
  try {
    classrooms.value = await classroomService.getAll();
  } catch (error) {
    showNotification(error.message || '获取教室列表失败', 'error');
  } finally {
    isLoading.value = false;
  }
};

// 组件挂载时获取数据
onMounted(fetchClassrooms);

// 重置表单
const resetForm = () => {
  editableClassroom.value = getNewEditableClassroom();
};

// 打开新增模态框
const openAddModal = () => {
  resetForm();
  isEditing.value = false;
  showModal.value = true;
};

// 打开编辑模态框
const openEditModal = (classroom) => {
  // 使用深拷贝，避免直接修改列表中的数据
  editableClassroom.value = { ...classroom };
  isEditing.value = true;
  showModal.value = true;
};

// 关闭模态框
const closeModal = () => {
  if (isSubmitting.value) return; // 防止提交过程中关闭
  showModal.value = false;
};

// 处理表单提交
const handleSubmit = async () => {
  if (!editableClassroom.value.name || !editableClassroom.value.type || editableClassroom.value.capacity <= 0) {
    showNotification('请填写所有必填项，并确保容量为正数。', 'error');
    return;
  }

  isSubmitting.value = true;
  try {
    const payload = { ...editableClassroom.value };

    if (isEditing.value) {
      await classroomService.update(payload.id, payload);
      showNotification('教室信息更新成功！', 'success');
    } else {
      await classroomService.create(payload);
      showNotification('教室创建成功！', 'success');
    }
    closeModal();
    await fetchClassrooms(); // 重新加载数据
  } catch (error) {
    showNotification(error.message || '操作失败', 'error');
  } finally {
    isSubmitting.value = false;
  }
};

// 处理删除操作
const handleDelete = async (id) => {
  if (!confirm('确定要删除这个教室吗？如果该教室已被课程安排使用，您可能需要先解除关联。')) {
    return;
  }

  try {
    await classroomService.delete(id);
    showNotification('教室删除成功！', 'success');
    await fetchClassrooms(); // 重新加载数据
  } catch (error) {
    showNotification(error.message || '删除失败，可能该教室正在被使用', 'error');
  }
};
</script>

<style scoped>
/* 页面容器和头部样式与项目其他页面保持一致 */
.page-container {
  padding: 24px 32px;
  height: 100%;
  box-sizing: border-box;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 2em;
  margin: 0;
  font-weight: 600;
  color: var(--color-text-primary);
}

.add-btn {
  background-color: var(--color-primary);
  color: white;
  border: none;
  padding: 10px 20px;
  font-size: 1em;
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: opacity 0.2s;
}

.add-btn:hover {
  opacity: 0.9;
}

.content-card {
  background-color: #fff;
  padding: 24px;
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
}

.loading-indicator,
.no-data-cell {
  text-align: center;
  padding: 40px;
  font-size: 1.1em;
  color: var(--color-text-secondary);
}

/* 数据表格样式 */
.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th, .data-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid var(--color-border);
  vertical-align: middle;
}

.data-table th {
  background-color: #f8f9fa;
  font-weight: 600;
}

.action-btn {
  margin-right: 8px;
  padding: 6px 12px;
  border-radius: 4px;
  border: 1px solid transparent;
  cursor: pointer;
  font-size: 0.9em;
  font-weight: 500;
  transition: background-color 0.2s;
}

.action-btn.edit {
  color: var(--color-primary);
  background-color: var(--color-primary-light);
}
.action-btn.edit:hover {
  background-color: #d2e4f5;
}

.action-btn.delete {
  color: var(--color-danger);
  background-color: #fdf2f2;
}
.action-btn.delete:hover {
  background-color: #fbdfe2;
}


/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  padding: 32px;
  border-radius: var(--border-radius);
  width: 500px;
  max-width: 90%;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
}

.modal-content h2 {
  margin-top: 0;
  margin-bottom: 24px;
  font-size: 1.5em;
  font-weight: 600;
  color: var(--color-text-primary);
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #495057;
}

.required {
  color: var(--color-danger);
  margin-left: 4px;
}

.form-group input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--border-radius);
  box-sizing: border-box;
  font-family: inherit;
  font-size: 1em;
  background-color: #fdfdff;
  outline: none;
  transition: all var(--transition-speed) ease;
}

.form-group input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-light);
}

.modal-actions {
  margin-top: 32px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.modal-actions button {
  padding: 10px 24px;
  border-radius: var(--border-radius);
  border: 1px solid transparent;
  font-size: 1em;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.modal-actions .cancel-btn {
  background-color: #f1f3f5;
  border-color: #dee2e6;
  color: var(--color-text-primary);
}
.modal-actions .cancel-btn:hover {
  background-color: #e9ecef;
}

.modal-actions .submit-btn {
  background-color: var(--color-primary);
  color: white;
  border-color: var(--color-primary);
}
.modal-actions .submit-btn:hover {
  opacity: 0.9;
}
.modal-actions .submit-btn:disabled {
  background-color: #a0c3e2;
  border-color: #a0c3e2;
  cursor: not-allowed;
}

</style>