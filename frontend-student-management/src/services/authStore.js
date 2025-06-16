import { ref } from 'vue';

// 创建一个响应式的 ref，它的初始值基于 localStorage 中是否存在 token
export const isLoggedIn = ref(!!localStorage.getItem('token'));