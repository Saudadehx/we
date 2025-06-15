<template>
  <div class="student-list-container">
    <h2>学生列表</h2>
    <button @click="goToAddStudent" class="add-student-btn">添加新学生</button>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-if="error" class="error-message">{{ error }}</div>

    <table v-if="!loading && students.length > 0">
      <thead>
      <tr>
        <th>学号</th>
        <th>姓名</th>
        <th>性别</th>
        <th>出生日期</th>
        <th>班级</th>
        <th>操作</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="student in students" :key="student.id">
        <td>{{ student.studentId }}</td>
        <td>{{ student.name }}</td>
        <td>{{ student.gender }}</td>
        <td>{{ student.dateOfBirth }}</td>
        <td>{{ student.className }}</td>
        <td>
          <button @click="editStudent(student.id)" class="action-btn edit-btn">编辑</button>
          <button @click="confirmDeleteStudent(student.id)" class="action-btn delete-btn">删除</button>
        </td>
      </tr>
      </tbody>
    </table>

    <p v-if="!loading && students.length === 0 && !error">暂无学生信息，请先添加。</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import studentApiService from '@/services/studentApiService'; // 引入真实的API服务

const students = ref([]);
const loading = ref(true);
const error = ref('');
const router = useRouter();

/**
 * 从后端获取学生列表
 */
const fetchStudents = async () => {
  loading.value = true;
  error.value = '';
  try {
    const response = await studentApiService.getAllStudents();
    students.value = response.data;
  } catch (err) {
    console.error('获取学生列表失败:', err);
    error.value = '获取学生列表失败，请确保后端服务正在运行。';
  } finally {
    loading.value = false;
  }
};

// 组件加载时自动获取数据
onMounted(() => {
  fetchStudents();
});

/**
 * 跳转到添加学生页面
 */
const goToAddStudent = () => {
  router.push('/students/new');
};

/**
 * 跳转到编辑学生页面
 * @param {number} id - 学生ID
 */
const editStudent = (id) => {
  router.push(`/students/edit/${id}`);
};

/**
 * 确认并删除学生
 * @param {number} id - 学生ID
 */
const confirmDeleteStudent = async (id) => {
  if (window.confirm('您确定要删除该学生吗？此操作不可撤销。')) {
    try {
      await studentApiService.deleteStudent(id);
      // 删除成功后，重新加载列表以刷新界面
      await fetchStudents();
    } catch (err) {
      console.error('删除学生失败:', err);
      error.value = '删除学生失败，请稍后再试。';
    }
  }
};
</script>

<style scoped>
.student-list-container {
  padding: 20px;
  font-family: Arial, sans-serif;
  max-width: 1000px;
  margin: 20px auto;
}
.add-student-btn {
  margin-bottom: 15px;
  padding: 10px 15px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}
.add-student-btn:hover {
  background-color: #45a049;
}
table {
  width: 100%;
  border-collapse: collapse;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
th, td {
  border: 1px solid #ddd;
  padding: 12px;
  text-align: left;
}
th {
  background-color: #f2f2f2;
}
tr:nth-child(even) {
  background-color: #f9f9f9;
}
.action-btn {
  margin-right: 5px;
  padding: 5px 10px;
  border: none;
  border-radius: 3px;
  cursor: pointer;
}
.edit-btn {
  background-color: #2196F3;
  color: white;
}
.delete-btn {
  background-color: #f44336;
  color: white;
}
.loading, .error-message {
  margin-top: 20px;
  font-style: italic;
  text-align: center;
  font-size: 18px;
}
.error-message {
  color: red;
  padding: 10px;
  border: 1px solid red;
  background-color: #ffebee;
  border-radius: 4px;
}
</style>