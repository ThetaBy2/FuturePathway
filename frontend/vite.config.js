import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Dev server proxies /api → Spring Boot so no CORS issues during development.
// (The backend ALSO allows http://localhost:5173 via CORS for direct calls.)
export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0',
    port: 5173,
    // Allow cloud preview hosts (e.g. *.e2b.app) besides localhost.
    allowedHosts: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
