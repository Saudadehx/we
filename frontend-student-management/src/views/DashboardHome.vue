<template>
  <div class="dashboard-home">
    <div class="page-header">
      <h1>系统数据概览</h1>
      <p>实时洞察系统核心指标</p>
    </div>

    <div class="stats-container">
      <div class="stat-card card-students">
        <div class="card-content">
          <div class="stat-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" fill="currentColor" viewBox="0 0 16 16"><path d="M15 14s1 0 1-1-1-4-5-4-5 3-5 4 1 1 1 1h8zm-7.978-1A.261.261 0 0 1 7 12.996c.001-.264.167-1.03.76-1.72C8.312 10.629 9.282 10 11 10c1.717 0 2.687.63 3.24 1.276.593.69.758 1.457.76 1.72l-.008.002a.274.274 0 0 1-.014.002H7.022zM11 7a2 2 0 1 0 0-4 2 2 0 0 0 0 4zm3-2a3 3 0 1 1-6 0 3 3 0 0 1 6 0zM6.957 12.287c-.545.636-.99 1.17-1.255 1.65a.5.5 0 0 1-.415.263H2.5a.5.5 0 0 1 0-1h1.043c.101-.16.208-.322.321-.479L5 11.231l.008-.002A.274.274 0 0 1 5 11.23zM5 7a2 2 0 1 0 0-4 2 2 0 0 0 0 4zm0-2a3 3 0 1 1-6 0 3 3 0 0 1 6 0z"/></svg>
          </div>
          <div class="stat-text">
            <div class="stat-value">{{ stats.totalStudents }}</div>
            <div class="stat-label">总学生数</div>
          </div>
        </div>
      </div>

      <div class="stat-card card-grades">
        <div class="card-content">
          <div class="stat-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" fill="currentColor" viewBox="0 0 16 16"><path d="M9.283.022a.5.5 0 0 1 .234.052l6 3.5a.5.5 0 0 1 .028.91l-6 3.5a.5.5 0 0 1-.577-.028l-6-3.5a.5.5 0 0 1 .028-.91l6-3.5a.5.5 0 0 1 .315-.022zM8 4.204L1.923 8 8 11.796 14.077 8 8 4.204z"/><path d="M1.132 9.463a.5.5 0 0 1 .028.91l6 3.5a.5.5 0 0 1 0 .868l-6 3.5a.5.5 0 0 1-.577-.432V10.37a.5.5 0 0 1 .28-.463zM7.5 10.37v5.868l5.868-3.423a.5.5 0 0 1 .28-.463V9.463a.5.5 0 0 1-.28-.463L7.5 10.37z"/></svg>
          </div>
          <div class="stat-text">
            <div class="stat-value">{{ stats.totalGrades }}</div>
            <div class="stat-label">年级总数</div>
          </div>
        </div>
      </div>

      <div class="stat-card card-classes">
        <div class="card-content">
          <div class="stat-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" fill="currentColor" viewBox="0 0 16 16"><path d="M6 1v3H1V1h5zM1 0a1 1 0 0 0-1 1v3a1 1 0 0 0 1 1h5a1 1 0 0 0 1-1V1a1 1 0 0 0-1-1H1zm14 12v3h-5v-3h5zm-5-1a1 1 0 0 0-1 1v3a1 1 0 0 0 1 1h5a1 1 0 0 0 1-1v-3a1 1 0 0 0-1-1h-5zM6 8v7H1V8h5zM1 7a1 1 0 0 0-1 1v7a1 1 0 0 0 1 1h5a1 1 0 0 0 1-1V8a1 1 0 0 0-1-1H1zm14-6v7h-5V1h5zM10 0a1 1 0 0 0-1 1v7a1 1 0 0 0 1 1h5a1 1 0 0 0 1-1V1a1 1 0 0 0-1-1h-5z"/></svg>
          </div>
          <div class="stat-text">
            <div class="stat-value">{{ stats.totalClasses }}</div>
            <div class="stat-label">班级总数</div>
          </div>
        </div>
      </div>

      <div class="stat-card card-majors">
        <div class="card-content">
          <div class="stat-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" fill="currentColor" viewBox="0 0 16 16"><path d="M8.211 2.047a.5.5 0 0 0-.422 0l-7.5 3.5a.5.5 0 0 0 .025.917l7.5 3a.5.5 0 0 0 .372 0l7.5-3a.5.5 0 0 0 .025-.917l-7.5-3.5zM8 5.482 2.19 8 8 10.518 13.81 8 8 5.482z"/><path d="M15.76 8.574a.5.5 0 0 0-.05.025l-7.5 3a.5.5 0 0 0 0 .802l7.5 3a.5.5 0 0 0 .526-.829l-7.202-2.882L15.76 8.6zM1 8.574a.5.5 0 0 1 .05.025l7.5 3a.5.5 0 0 1 0 .802l-7.5 3a.5.5 0 0 1-.526-.829l7.202-2.882L1 8.6z"/></svg>
          </div>
          <div class="stat-text">
            <div class="stat-value">{{ stats.totalMajors }}</div>
            <div class="stat-label">专业总数</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { studentService } from '@/services/apiService';

const stats = ref({ totalStudents: 0, totalClasses: 0, totalGrades: 0, totalMajors: 0 });

onMounted(async () => {
  try {
    const response = await studentService.getStats();
    stats.value = response;
  } catch (error) {
    console.error("获取统计数据失败:", error);
  }
});
</script>

<style scoped>
.dashboard-home {
  padding: 2rem 3rem;
  width: 100%;
  animation: fadeIn 0.8s ease-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  margin-bottom: 2.5rem;
}
.page-header h1 {
  font-size: 2.2em;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}
.page-header p {
  font-size: 1.1em;
  color: #7f8c8d;
}

.stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 2rem;
}

.stat-card {
  border-radius: 12px;
  color: white;
  padding: 1.5rem;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}
.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 20px rgba(0,0,0,0.12);
}

.card-content {
  display: flex;
  align-items: center;
  position: relative;
  z-index: 2;
}
.stat-icon {
  margin-right: 1.5rem;
  opacity: 0.8;
}
.stat-icon svg {
  width: 48px;
  height: 48px;
}
.stat-text {
  text-align: left;
}
.stat-value {
  font-size: 2.2em;
  font-weight: 700;
  line-height: 1;
}
.stat-label {
  font-size: 1em;
  opacity: 0.9;
  margin-top: 0.25rem;
}

/* 定义不同卡片的颜色和背景渐变 */
.card-students { background: linear-gradient(135deg, #3498db, #2980b9); }
.card-grades { background: linear-gradient(135deg, #2ecc71, #27ae60); }
.card-classes { background: linear-gradient(135deg, #e67e22, #d35400); }
.card-majors { background: linear-gradient(135deg, #9b59b6, #8e44ad); }

/* 卡片背景装饰图形 */
.stat-card::before {
  content: '';
  position: absolute;
  top: -50px;
  right: -50px;
  width: 150px;
  height: 150px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  opacity: 0.5;
  transition: all 0.5s ease;
  z-index: 1;
}
.stat-card:hover::before {
  transform: scale(1.5);
  opacity: 0;
}
</style>