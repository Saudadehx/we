<template>
  <Transition name="fade">
    <div v-if="notification.visible" class="notification-container" :class="notification.type">
      {{ notification.message }}
    </div>
  </Transition>
</template>

<script setup>
import { notification } from '@/services/notificationStore.js';
</script>

<style scoped>
.notification-container {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  padding: 15px 25px;
  border-radius: 8px;
  color: white;
  font-size: 1.1em;
  z-index: 9999;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}
.success { background-color: #198754; }
.error { background-color: #dc3545; }

/* Vue Transition 的淡入淡出效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>