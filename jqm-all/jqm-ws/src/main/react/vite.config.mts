import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        port: 3000,
        open: true,
        proxy: {
            "/ws": {
                target: `http://localhost:${process.env.JQM_CONSOLE_PORT}`,
                changeOrigin: true,
                ws: true,
            },
            "/api/perfData": {
                target: "http://127.0.0.1:12800",
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api\/perfData/, "/browser/perfData"),
            },

            "/api/sw": {
                target: "http://127.0.0.1:12800",
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api\/sw/, ""),
            },
        },
    },
    build: {
        outDir: "build",
        sourcemap: true,
    },
    base: "./",
});
