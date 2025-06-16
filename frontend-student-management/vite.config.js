import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path'; // 引入 Node.js 的 path 模块

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // 设置别名，告诉 Vite @ 指向什么
      '@': path.resolve(__dirname, './src')
    }
  },
  // --- 添加这个 server 配置块 ---
  server: {
    port: 5173, // 我们强制Vite使用5173端口
    strictPort: true, // 如果5173端口被占用，则直接退出，而不是尝试其他端口
  }
});