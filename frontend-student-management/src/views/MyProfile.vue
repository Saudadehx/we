<template>
  <div class="profile-container">
    <div v-if="isLoading" class="loading-placeholder">
      <p>正在加载您的个人信息...</p>
    </div>
    <div v-else-if="studentData">
      <StudentDetail
          :student="studentData"
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
const isLoading = ref(true);
const isSaving = ref(false);

const fetchProfile = async () => {
  isLoading.value = true;
  try {
    studentData.value = await studentService.getMyProfile();
  } catch (error) {
    showNotification('加载个人信息失败: ' + error.message, 'error');
    studentData.value = null;
  } finally {
    isLoading.value = false;
  }
};

onMounted(fetchProfile);

const handleProfileUpdate = async (formData) => {
  if (isSaving.value) return;
  isSaving.value = true;

  try {
    // ✨ 简化：直接传递整个 formData。
    // 后端服务层 `updateStudentProfile` 会智能地只提取需要更新的字段。
    await studentService.updateMyProfile(formData);

    showNotification('个人信息更新成功！', 'success');

    // 保存成功后，重新从服务器获取最新的数据
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