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
    <p v-if="!loading && students.length === 0 && !error">暂无学生信息。</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import studentApiService from '@/services/studentApiService'; // 稍后创建

const students = ref([]);
const loading = ref(true);
const error = ref('');
const router = useRouter();

// 模拟API调用，实际应替换为 studentApiService
const fetchStudents = async () => {
loading.value = true;
    error.value = '';
    try {
      const response = await studentApiService.getAllStudents();
      students.value = response.data;
      if (students.value.length === 0) {
        console.log("No students fetched from API.");
      }
    } catch (err) {
      console.error('获取学生列表失败:', err);
      error.value = err.response?.data?.message || err.message || '获取学生列表失败，请稍后再试。';
    } finally {
      loading.value = false;
    }
      // { id: 1, studentId: 'S1001', name: '张三', gender: '男', dateOfBirth: '2000-01-15', className: '计算机科学1班' },
      // { id: 2, studentId: 'S1002', name: '李四', gender: '女', dateOfBirth: '2001-03-20', className: '软件工程2班' }
    ];
    if (students.value.length === 0) {
        console.log("No students fetched (using placeholder).");
    }
  } catch (err) {
    console.error('获取学生列表失败:', err);
    error.value = '获取学生列表失败，请稍后再试。';
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchStudents();
});

const goToAddStudent = () => {
  router.push('/students/new'); // 路由将在下一步配置
};

const editStudent = (id) => {
  router.push(); // 路由将在下一步配置
};

const confirmDeleteStudent = async (id) => {
if (window.confirm('确定要删除该学生吗？')) {
      loading.value = true;
      error.value = '';
      try {
        await studentApiService.deleteStudent(id);
        await fetchStudents();
      } catch (err) {
        console.error('删除学生失败:', err);
        error.value = err.response?.data?.message || err.message || '删除学生失败。';
      } finally {
        loading.value = false;
      }
    }
    } catch (err) {
      console.error('删除学生失败:', err);
      error.value = '删除学生失败。';
    }
  }
};
</script>

<style scoped>
.student-list-container {
  padding: 20px;
  font-family: Arial, sans-serif;
}
.add-student-btn {
  margin-bottom: 15px;
  padding: 10px 15px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.add-student-btn:hover {
  background-color: #45a049;
}
table {
  width: 100%;
  border-collapse: collapse;
}
th, td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}
th {
  background-color: #f2f2f2;
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
}
.error-message {
  color: red;
}
</style>
