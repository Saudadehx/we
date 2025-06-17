<template>
  <div id="app-layout" :class="{ 'full-page': isLoginPage }">
    <Notification />

    <Sidebar v-if="!isLoginPage" />

    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import Notification from '@/components/Notification.vue';
import Sidebar from '@/components/Sidebar.vue';

const route = useRoute();
const isLoginPage = computed(() => route.name === 'Login');
</script>

<style scoped>
#app-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden; /* 防止整体布局出现滚动条，内部区域再独立滚动 */
  background-color: var(--color-background);
}

/* 当是登录页时，让布局占满整个页面 */
#app-layout.full-page {
  display: block; /* 覆盖 flex 布局 */
}

.main-content {
  flex-grow: 1; /* 占据剩余的所有空间 */
  /* 允许内容区域独立滚动 */
  overflow-y: auto;
  height: 100vh; /* 确保 main-content 自身的高度撑满 */
}
</style>