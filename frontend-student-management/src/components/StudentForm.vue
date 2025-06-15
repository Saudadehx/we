<template>
  <div class="student-form-container">
    <h2>{{ formTitle }}</h2>
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label for="studentId">学号:</label>
        <input type="text" id="studentId" v-model="student.studentId" required :disabled="isEditMode">
      </div>
      <div class="form-group">
        <label for="name">姓名:</label>
        <input type="text" id="name" v-model="student.name" required>
      </div>
      <div class="form-group">
        <label for="gender">性别:</label>
        <select id="gender" v-model="student.gender" required>
          <option value="">请选择</option>
          <option value="男">男</option>
          <option value="女">女</option>
          <option value="其他">其他</option>
        </select>
      </div>
      <div class="form-group">
        <label for="dateOfBirth">出生日期:</label>
        <input type="date" id="dateOfBirth" v-model="student.dateOfBirth" required>
      </div>
      <div class="form-group">
        <label for="className">班级名称:</label>
        <input type="text" id="className" v-model="student.className" required>
      </div>
      <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
      <div class="form-actions">
        <button type="submit" class="submit-btn">{{ submitButtonText }}</button>
        <button type="button" @click="cancel" class="cancel-btn">取消</button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import studentApiService from '@/services/studentApiService'; // 稍后创建

const props = defineProps({
  id: String // 从路由参数接收，用于编辑模式
});

const student = ref({
  studentId: '',
  name: '',
  gender: '',
  dateOfBirth: '',
  className: ''
});
const errorMessage = ref('');
const router = useRouter();
const route = useRoute(); // Using route to access params directly if needed, though props is preferred for defined params

const isEditMode = computed(() => !!props.id);
const formTitle = computed(() => isEditMode.value ? '编辑学生信息' : '添加新学生');
const submitButtonText = computed(() => isEditMode.value ? '更新' : '创建');

// 模拟API调用
const fetchStudentDetails = async (studentIdParam) => { // Changed param name to avoid conflict with ref 'student'
  if (!studentIdParam) return;
  try {
    // const response = await studentApiService.getStudentById(studentIdParam);
    console.log();
    await new Promise(resolve => setTimeout(resolve, 300));
    // student.value = { id: studentIdParam, studentId: 'S1001_EDIT', name: '张三编辑', gender: '男', dateOfBirth: '2000-01-15', className: '计算机科学1班' };
  } catch (error) {
    console.error('获取学生详情失败:', error);
    errorMessage.value = '获取学生详情失败。';
  }
};

onMounted(() => {
  if (isEditMode.value) {
    fetchStudentDetails(props.id);
  }
});

watch(() => props.id, (newId) => {
  if (isEditMode.value && newId) { // Check newId as well
    fetchStudentDetails(newId);
  } else if (!newId) {
    student.value = { studentId: '', name: '', gender: '', dateOfBirth: '', className: '' };
    errorMessage.value = '';
  }
}, { immediate: true }); // Add immediate: true if needed, but onMounted handles initial load


const handleSubmit = async () => {
errorMessage.value = '';
  try {
    if (isEditMode.value) {
      await studentApiService.updateStudent(props.id, student.value);
    } else {
      await studentApiService.createStudent(student.value);
    }
    router.push({ name: 'StudentList' });
  } catch (err) {
    console.error('提交学生信息失败:', err);
    if (err.response && err.response.data) {
      if (typeof err.response.data === 'string') {
        errorMessage.value = err.response.data;
      } else if (typeof err.response.data === 'object' && !(err.response.data instanceof Blob)) {
        const backendErrors = err.response.data;
        let messages = [];
        for (const key in backendErrors) {
            messages.push(`${key}: ${backendErrors[key]}`);
        }
        errorMessage.value = messages.join('
'); // Literal newline for <pre> or white-space: pre-line
      } else {
         errorMessage.value = '提交失败，请检查输入信息。';
      }
    } else {
      errorMessage.value = err.message || '提交失败，请检查输入信息。';
    }
  }
    } else {
      // await studentApiService.createStudent(student.value);
      console.log("Simulating create student:", student.value);
    }
    router.push('/');
  } catch (error) {
    console.error('提交学生信息失败:', error);
    errorMessage.value = error.response?.data?.message || error.message || '提交失败，请检查输入信息。';
    if (error.response?.data && typeof error.response.data === 'object' && !(error.response.data instanceof Blob)) { // Check it's not a Blob
        const backendErrors = error.response.data;
        let messages = [];
        for (const key in backendErrors) {
            messages.push();
        }
        if (messages.length > 0) {
            errorMessage.value = messages.join('\n'); // Use \n for literal \n in string, then display with white-space: pre-line
        }
    }
  }
};

const cancel = () => {
  router.push('/');
};
</script>

<style scoped>
.student-form-container {
  max-width: 500px;
  margin: 20px auto;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.form-group {
  margin-bottom: 15px;
}
.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}
.form-group input[type="text"],
.form-group input[type="date"],
.form-group select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}
.form-actions {
  margin-top: 20px;
  text-align: right;
}
.submit-btn, .cancel-btn {
  padding: 10px 20px;
  margin-left: 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.submit-btn {
  background-color: #4CAF50;
  color: white;
}
.cancel-btn {
  background-color: #f44336;
  color: white;
}
.error-message {
  color: red;
  margin-bottom: 15px;
  white-space: pre-line;
}
</style>
