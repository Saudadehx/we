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
import Sidebar from '@/components/Sidebar.vue'; // 导入新的侧边栏组件

const route = useRoute();
const isLoginPage = computed(() => route.name === 'Login');
</script>

<style scoped>
#app-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background-color: var(--color-background);
}

/* 当是登录页时，让布局占满整个页面 */
#app-layout.full-page {
  display: block;
}

.main-content {
  flex-grow: 1;
  /* 允许内容区域独立滚动 */
  overflow-y: auto;
  height: 100vh;
}
</style>