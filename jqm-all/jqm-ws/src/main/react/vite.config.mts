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
        },
    },
    build: {
        outDir: "build",
        sourcemap: true,
    },
    base: "./",
});
