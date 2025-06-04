// vite.config.js
import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import vueDevTools from "vite-plugin-vue-devtools";
import tailwindcss from "@tailwindcss/vite";

export default defineConfig({
  plugins: [vue(), vueDevTools(), tailwindcss()],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  server: {
    host: "0.0.0.0",
    port: 3000,
    watch: {
      usePolling: true,
    },
    proxy: {
      "/app": {
        target: "http://backend:8080",
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/app/, ""),
      },
      "/auth": {
        target: "http://auth-server:8081",
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/auth/, ""),
      },
      "/dek": {
        target: "http://dek:8082",
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/dek/, ""),
      },
      "/kms": {
        target: "http://kms:8000",
        changeOrigin: true,
        secure: false,
        rewrite: (path) => path.replace(/^\/kms/, ""),
      },
    },
  },
});
