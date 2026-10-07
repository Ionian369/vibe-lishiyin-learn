import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// 开发服务器配置：/api 请求代理到本地 Spring Boot 后端，规避跨域
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
