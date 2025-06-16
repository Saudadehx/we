<template>
  <div class="page-container">
    <header class="page-header">
      <h1>{{ courseName }} - 成绩录入</h1>
      <button @click="router.back()" class="back-btn">返回课程列表</button>
    </header>

    <div v-if="isLoading" class="loading-indicator">正在加载学生名单...</div>

    <div v-else class="content-card">
      <table class="data-table grade-table">
        <thead>
        <tr>
          <th>学号</th>
          <th>姓名</th>
          <th class="score-col">成绩</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="item in enrollments" :key="item.enrollmentId">
          <td>{{ item.studentId }}</td>
          <td>{{ item.studentName }}</td>
          <td class="score-col">
            <input
                type="number"
                class="grade-input"
                v-model.lazy="item.score"
                @change="updateGrade(item.enrollmentId, item.score)"
                placeholder="-"
            >
          </td>
        </tr>
        <tr v-if="enrollments.length === 0">
          <td colspan="3" style="text-align: center;">暂无学生选修此课程。</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { enrollmentService } from '@/services/apiService';
import { showNotification } from '@/services/notificationStore';

// 通过 defineProps 接收路由传递过来的课程ID
const props = defineProps({
  id: { type: String, required: true }
});

const router = useRouter();
const enrollments = ref([]);
const courseName = ref('课程');
const isLoading = ref(true);

const fetchEnrollments = async () => {
  isLoading.value = true;
  try {
    // 调用API获取这门课的选课名单和成绩
    const data = await enrollmentService.getForCourse(props.id);
    enrollments.value = data;
    // 从返回的数据中获取课程名称，以在标题中显示
    if (data.length > 0) {
      courseName.value = data[0].courseName;
    }
  } catch (error) {
    showNotification(error.message || '获取学生列表失败', 'error');
  } finally {
    isLoading.value = false;
  }
};

const updateGrade = async (enrollmentId, newScore) => {
  // 如果输入框被清空，我们传递 null 到后端
  const scoreToUpdate = newScore === '' || newScore === null ? null : Number(newScore);

  try {
    await enrollmentService.updateGrade(enrollmentId, scoreToUpdate);
    showNotification('成绩保存成功！', 'success');
  } catch (error) {
    showNotification(error.message || '保存失败，请重试', 'error');
    // 保存失败时，最好重新从服务器获取数据，以撤销前端的错误修改
    fetchEnrollments();
  }
};

onMounted(fetchEnrollments);
</script>

<style scoped>
@import '@/assets/styles/common-page.css';

.back-btn {
  background-color: #6c757d;
  color: white;
  border: none;
  padding: 10px 20px;
  font-size: 1em;
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: opacity 0.2s;
}
.back-btn:hover {
  opacity: 0.9;
}

.grade-table .score-col {
  width: 120px;
  text-align: center;
}

.grade-table td.score-col {
  text-align: center;
}

.grade-input {
  width: 80px;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  text-align: center;
  font-size: 1em;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.grade-input:focus {
  border-color: var(--color-primary);
  outline: none;
  box-shadow: 0 0 0 2px var(--color-primary-light);
}
</style>