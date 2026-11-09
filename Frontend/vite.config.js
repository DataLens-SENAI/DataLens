import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    tailwindcss(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  // Em desenvolvimento, /api/... vai para a API Java (sem precisar de CORS)
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        rewrite: (caminho) => caminho.replace(/^\/api/, ''),
      },
    },
  },
})
