import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import svgr  from '@svgr/rollup'
import fs    from 'fs'
import path  from 'path'

export default defineConfig({
  plugins: [react(), svgr()],
  server: {
    port: 5173,   // 기본값
    https: {
      key:  fs.readFileSync(path.resolve(__dirname, 'certs/localhost+2-key.pem')),
      cert: fs.readFileSync(path.resolve(__dirname, 'certs/localhost+2.pem')),
    },
    
  },
})
