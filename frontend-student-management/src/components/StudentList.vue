<template>
  <div class="table-container">
    <h2>学生信息管理 (内联编辑模式)</h2>
    <div class="actions-header">
      <button v-if="isAdmin" @click="addNewStudentRow" class="add-btn">添加新行</button>
    </div>
    <table>
      <thead>
      <tr>
        <th>照片</th>
        <th>姓名</th>
        <th>学号</th>
        <th>专业</th>
        <th>班级</th>
        <th>绩点</th>
        <th>操作</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="student in students" :key="student.id">
        <td><img :src="student.photoUrl || 'default-avatar.png'" alt="avatar" class="avatar"></td>

        <td>
          <span v-if="editingId !== student.id">{{ student.name }}</span>
          <input v-else type="text" v-model="editableStudent.name" class="inline-input">
        </td>

        <td>
          <span v-if="editingId !== student.id">{{ student.studentId }}</span>
          <input v-else type="text" v-model="editableStudent.studentId" class="inline-input">
        </td>

        <td>
          <span v-if="editingId !== student.id">{{ student.major }}</span>
          <input v-else type="text" v-model="editableStudent.major" class="inline-input">
        </td>

        <td>
          <span v-if="editingId !== student.id">{{ student.className }}</span>
          <input v-else type="text" v-model="editableStudent.className" class="inline-input">
        </td>

        <td>
          <span v-if="editingId !== student.id">{{ student.gpa }}</span>
          <input v-else type="number" step="0.01" v-model="editableStudent.gpa" class="inline-input">
        </td>

        <td>
          <div v-if="editingId !== student.id">
            <button @click="startEditing(student)" class="action-btn edit-btn">编辑</button>
            <button @click="confirmDeleteStudent(student.id)" class="action-btn delete-btn">删除</button>
          </div>
          <div v-else>
            <button @click="saveChanges(student.id)" class="action-btn save-btn">保存</button>
            <button @click="cancelEditing" class="action-btn cancel-btn">取消</button>
          </div>
        </td>
      </tr>
      </tbody>
    </table>
    <div v-if="loading">加载中...</div>
    <div v-if="error" class="error-message">{{ error }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { studentService, authService } from '@/services/apiService';

const students = ref([]);
const loading = ref(true);
const error = ref('');

// --- 内联编辑核心状态 ---
const editingId = ref(null); // 正在编辑的学生ID
const editableStudent = ref(null); // 正在编辑的学生数据副本

const isAdmin = computed(() => authService.getUserRole() === 'ADMIN');

const fetchStudents = async () => {
  loading.value = true;
  error.value = '';
  try {
    const response = await studentService.getAllStudents();
    students.value = response.data;
  } catch (err) {
    console.error('获取学生列表失败:', err);
    error.value = '获取学生列表失败。';
  } finally {
    loading.value = false;
  }
};

onMounted(fetchStudents);

// 开始编辑
const startEditing = (student) => {
  editingId.value = student.id;
  // 创建一个副本进行编辑
  editableStudent.value = { ...student };
};

// 取消编辑
const cancelEditing = () => {
  editingId.value = null;
  editableStudent.value = null;
};

// 保存更改
const saveChanges = async (id) => {
  if (!editableStudent.value) return;

  // 如果是新创建的行 (id为'new')
  if (id === 'new') {
    try {
      await studentService.createStudent(editableStudent.value);
      alert('学生创建成功！');
    } catch(err) {
      alert('创建失败: ' + (err.response?.data || err.message));
      return;
    }
  } else { // 如果是更新现有行
    try {
      await studentService.updateStudent(id, editableStudent.value);
      alert('学生信息更新成功！');
    } catch (err) {
      alert('更新失败: ' + (err.response?.data || err.message));
      return;
    }
  }

  cancelEditing();
  await fetchStudents();
};

// 删除学生
const confirmDeleteStudent = async (id) => {
  if (id === 'new') { // 如果是还没保存的新行
    students.value.shift(); // 从数组开头移除
    return;
  }
  if (window.confirm('确定要删除该学生吗？')) {
    try {
      await studentService.deleteStudent(id);
      await fetchStudents();
    } catch (err) {
      alert('删除失败，请稍后再试。');
    }
  }
};

// 添加一个用于编辑的新行
const addNewStudentRow = () => {
  if (editingId.value) {
    alert('请先完成当前的编辑！');
    return;
  }
  const newStudentTemplate = {
    id: 'new', // 临时ID
    name: '',
    studentId: '',
    major: '',
    className: '',
    gpa: 0.0,
    photoUrl: '',
    // 其他字段默认为空或默认值
    gender: '男',
    dateOfBirth: new Date().toISOString().split('T')[0]
  };
  students.value.unshift(newStudentTemplate); // 在列表顶部添加新行
  startEditing(newStudentTemplate);
};
</script>

<style scoped>
.table-container {
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}
.actions-header {
  text-align: right;
  margin-bottom: 15px;
}
.add-btn {
  background-color: #0d6efd;
  color: white;
  padding: 8px 15px;
}
table {
  width: 100%;
  border-collapse: collapse;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
th, td {
  border-bottom: 1px solid #ddd;
  padding: 12px;
  text-align: left;
}
th {
  background-color: #f8f9fa;
  font-weight: 600;
}
tr:hover {
  background-color: #f1f1f1;
}
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}
.inline-input {
  width: 100%;
  padding: 6px;
  border: 1px solid #0d6efd;
  border-radius: 4px;
  box-sizing: border-box;
}
.action-btn {
  margin-right: 5px;
  padding: 5px 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: white;
}
.edit-btn { background-color: #ffc107; color: #333;}
.delete-btn { background-color: #dc3545; }
.save-btn { background-color: #198754; }
.cancel-btn { background-color: #6c757d; }
</style>