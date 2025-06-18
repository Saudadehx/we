<template>
  <div class="page-container">
    <header class="page-header">
      <h1>系统设置</h1>
      <p>管理系统的全局参数和功能开关。</p>
    </header>

    <div class="content-card settings-panel">
      <div class="setting-item">
        <div class="setting-info">
          <h3 class="setting-title">学生选课/退课通道</h3>
          <p class="setting-description">
            开启后，学生将可以进入“选课中心”进行选课和退课操作。关闭后，学生将无法执行这些操作。
          </p>
        </div>
        <div class="setting-control">
          <label class="switch">
            <input type="checkbox" :checked="isCourseSelectionOpen" @change="toggleCourseSelection" :disabled="isUpdating">
            <span class="slider round"></span>
          </label>
          <span class="status-indicator" :class="{ 'is-open': isCourseSelectionOpen }">
            {{ statusText }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { apiService } from '@/services/systemSettingService'; // 将创建新文件
import { showNotification } from '@/services/notificationStore';

const isCourseSelectionOpen = ref(false);
const isLoading = ref(true);
const isUpdating = ref(false);

const statusText = computed(() => {
  if (isLoading.value) return '加载中...';
  return isCourseSelectionOpen.value ? '已开启' : '已关闭';
});

const fetchStatus = async () => {
  isLoading.value = true;
  try {
    const response = await apiService.getCourseSelectionStatus();
    isCourseSelectionOpen.value = response.isOpen;
  } catch (error) {
    showNotification('获取设置状态失败', 'error');
  } finally {
    isLoading.value = false;
  }
};

const toggleCourseSelection = async () => {
  isUpdating.value = true;
  const newState = !isCourseSelectionOpen.value;
  try {
    await apiService.setCourseSelectionStatus(newState);
    isCourseSelectionOpen.value = newState;
    showNotification(`选课通道已成功${newState ? '开启' : '关闭'}`, 'success');
  } catch (error) {
    showNotification('更新设置失败', 'error');
  } finally {
    isUpdating.value = false;
  }
};

onMounted(fetchStatus);
</script>

<style scoped>
.settings-panel {
  max-width: 800px;
  margin: auto;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 0;
  border-bottom: 1px solid var(--color-border);
}
.setting-item:last-child {
  border-bottom: none;
}

.setting-info {
  margin-right: 24px;
}

.setting-title {
  margin: 0 0 8px 0;
  font-size: 1.2em;
  color: var(--color-text-primary);
}

.setting-description {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 0.9em;
}

.setting-control {
  display: flex;
  align-items: center;
  gap: 16px;
}

.status-indicator {
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 0.9em;
  transition: all 0.3s ease;
}
.status-indicator.is-open {
  background-color: var(--color-success);
  color: white;
}
.status-indicator:not(.is-open) {
  background-color: #e9ecef;
  color: var(--color-text-secondary);
}

/* Switch toggle styles */
.switch {
  position: relative;
  display: inline-block;
  width: 60px;
  height: 34px;
}
.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}
.slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  transition: .4s;
}
.slider:before {
  position: absolute;
  content: "";
  height: 26px;
  width: 26px;
  left: 4px;
  bottom: 4px;
  background-color: white;
  transition: .4s;
}
input:checked + .slider {
  background-color: var(--color-primary);
}
input:focus + .slider {
  box-shadow: 0 0 1px var(--color-primary);
}
input:checked + .slider:before {
  transform: translateX(26px);
}
.slider.round {
  border-radius: 34px;
}
.slider.round:before {
  border-radius: 50%;
}
input:disabled + .slider {
  cursor: not-allowed;
  background-color: #e9ecef;
}
</style>