import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // 设置别名，告诉 Vite @ 指向什么
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    port: 5173, // 我们强制Vite使用5173端口
    strictPort: true, // 如果5173端口被占用，则直接退出，而不是尝试其他端口
    hmr: { // 确保热模块替换（HMR）配置正确，解决WebSocket连接问题
      clientPort: 5173, // 如果前端通过IP访问，这里可能需要设置为实际IP
      // host: 'localhost', // 如果有跨域问题，或在WSL/Docker中，可尝试设置为 '0.0.0.0' 或您的实际IP
    }
  }
});