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
          <option value="" disabled>请选择</option>
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
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import studentApiService from '@/services/studentApiService'; // 引入真实的API服务

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

// 动态计算属性，用于改变UI
const isEditMode = computed(() => !!props.id);
const formTitle = computed(() => isEditMode.value ? '编辑学生信息' : '添加新学生');
const submitButtonText = computed(() => isEditMode.value ? '更新' : '创建');

/**
 * 在编辑模式下，根据ID获取学生详情
 * @param {string} studentIdParam - 学生ID
 */
const fetchStudentDetails = async (studentIdParam) => {
  if (!studentIdParam) return;
  try {
    const response = await studentApiService.getStudentById(studentIdParam);
    student.value = response.data;
  } catch (error) {
    console.error('获取学生详情失败:', error);
    errorMessage.value = '获取学生详情失败，请检查ID是否正确。';
  }
};

// 组件加载时，如果是编辑模式，则获取数据
onMounted(() => {
  if (isEditMode.value) {
    fetchStudentDetails(props.id);
  }
});

/**
 * 处理表单提交（创建或更新）
 */
const handleSubmit = async () => {
  errorMessage.value = ''; // 重置错误信息
  try {
    if (isEditMode.value) {
      await studentApiService.updateStudent(props.id, student.value);
    } else {
      await studentApiService.createStudent(student.value);
    }
    router.push('/'); // 操作成功后，返回列表页
  } catch (err) {
    console.error('提交学生信息失败:', err);
    if (err.response && err.response.data) {
      // 尝试解析后端返回的错误信息
      const errorData = err.response.data;
      if (typeof errorData === 'string') {
        errorMessage.value = errorData;
      } else if (typeof errorData === 'object') {
        // 如果后端返回的是一个错误对象，例如 { "name": "姓名不能为空" }
        errorMessage.value = Object.values(errorData).join('\n');
      } else {
        errorMessage.value = '发生未知错误。';
      }
    } else {
      errorMessage.value = '操作失败，请检查网络或联系管理员。';
    }
  }
};

/**
 * 取消操作，返回列表页
 */
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
  box-sizing: border-box; /* 保证padding不会撑大宽度 */
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
  font-size: 16px;
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
  white-space: pre-line; /* 让\n换行符生效 */
  padding: 10px;
  border: 1px solid red;
  background-color: #ffebee;
  border-radius: 4px;
}
</style>