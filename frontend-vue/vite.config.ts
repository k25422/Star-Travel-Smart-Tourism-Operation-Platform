import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

export default defineConfig({
  plugins: [vue()],
  base: '/Star-Travel-Smart-Tourism-Operation-Platform/',
  build: {
    chunkSizeWarningLimit: 1200,
    rollupOptions: {
      output: {
        manualChunks: {
          "vue-vendor": ["vue", "vue-router", "pinia"],
          "element-plus": ["element-plus", "@element-plus/icons-vue"],
          charts: ["echarts"],
          three: ["three"],
          http: ["axios"]
        }
      }
    }
  },
  server: {
    proxy: {
      "/api": "http://localhost:8080"
    }
  }
});
