import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  server: {
    host: true, 
    port: 80,
    allowedHosts: ['preinter.castberry.kr']
  },
  plugins: [react()],
})
