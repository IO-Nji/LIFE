import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    open: false,
    proxy: {
      '/api': {
        target: 'http://localhost:8011', // Your API Gateway port
        changeOrigin: true,
        secure: false,
        // Remove the rewrite to keep /api prefix
        // rewrite: (path) => path,
      }
    }
  },
  // Production build configuration
  build: {
    outDir: 'dist',
    sourcemap: false,
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['react', 'react-dom'],
          router: ['react-router-dom']
        }
      }
    }
  },
  // Ensure relative paths work in production
  base: '/',
  // Define environment variables for build
  define: {
    __API_BASE__: JSON.stringify('/api')
  }
});
