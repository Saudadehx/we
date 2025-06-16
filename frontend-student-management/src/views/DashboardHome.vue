<template>
  <div class="dashboard-home">
    <h2>系统概览</h2>
    <div class="stats-container">
      <div class="stat-card">
        <div class="stat-icon students"></div>
        <div class="stat-value">{{ stats.totalStudents }}</div>
        <div class="stat-label">总学生数</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon grades"></div>
        <div class="stat-value">{{ stats.totalGrades }}</div>
        <div class="stat-label">年级总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon classes"></div>
        <div class="stat-value">{{ stats.totalClasses }}</div>
        <div class="stat-label">班级总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon majors"></div>
        <div class="stat-value">{{ stats.totalMajors }}</div>
        <div class="stat-label">专业总数</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { studentService } from '@/services/apiService';

const stats = ref({
  totalStudents: 0,
  totalClasses: 0,
  totalGrades: 0,
  totalMajors: 0,
});

onMounted(async () => {
  try {
    const response = await studentService.getStats();
    stats.value = response.data;
  } catch (error) {
    console.error("获取统计数据失败:", error);
  }
});
</script>

<style scoped>
.dashboard-home {
  padding: 30px;
  width: 100%;
}
h2 {
  font-size: 2em;
  margin-bottom: 30px;
  color: #333;
}
.stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 30px;
}
.stat-card {
  background-color: #fff;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  text-align: center;
}
.stat-value {
  font-size: 2.5em;
  font-weight: bold;
  color: #337ab7;
  margin: 10px 0;
}
.stat-label {
  font-size: 1.1em;
  color: #666;
}
</style>