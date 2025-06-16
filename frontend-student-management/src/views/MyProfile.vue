<template>
  <div class="profile-container">
    <div v-if="isLoading" class="loading-placeholder">
      <p>正在加载您的个人信息...</p>
    </div>
    <div v-else-if="studentData">
      <StudentDetail
          :student="studentData"
          :is-admin="false"
          view-mode="student"
          :is-saving="isSaving"
          @update-student="handleProfileUpdate"
      />
    </div>
    <div v-else class="loading-placeholder">
      <p>无法加载个人信息，请稍后重试。</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { studentService } from '@/services/apiService';
import { showNotification } from '@/services/notificationStore.js';
import StudentDetail from '@/components/StudentDetail.vue';

const studentData = ref(null);
const isLoading = ref(true); // 新增：用于控制初始加载状态
const isSaving = ref(false);  // 新增：用于控制“保存中”的状态

// 封装一个独立的获取数据的函数
const fetchProfile = async () => {
  isLoading.value = true;
  try {
    studentData.value = await studentService.getMyProfile();
  } catch (error) {
    showNotification('加载个人信息失败: ' + error.message, 'error');
    studentData.value = null; // 加载失败，确保不显示旧数据
  } finally {
    isLoading.value = false;
  }
};

// 页面加载时，调用此函数
onMounted(fetchProfile);

// 【重要】改造后的更新处理函数
const handleProfileUpdate = async (formData) => {
  if (isSaving.value) return; // 防止重复提交
  isSaving.value = true;

  try {
    const dataToUpdate = {
      password: formData.password,
      phoneNumber: formData.phoneNumber,
      email: formData.email
    };

    await studentService.updateMyProfile(dataToUpdate);

    showNotification('个人信息更新成功！', 'success');

    // 【关键】保存成功后，重新从服务器获取最新的数据，确保页面同步
    await fetchProfile();

  } catch (error) {
    showNotification(`更新失败: ${error.message}`, 'error');
  } finally {
    isSaving.value = false;
  }
};
</script>

<style scoped>
.profile-container {
  width: 100%;
  height: 100%;
}
.loading-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  font-size: 1.2em;
  color: var(--color-text-secondary);
}
</style>