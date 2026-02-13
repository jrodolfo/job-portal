import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  base: '/',
  build: {
    outDir: 'build'
  },
  test: {
    pool: 'threads',
    globals: true,
    environment: 'happy-dom',
    setupFiles: './src/test/setupTests.js'
  }
})
