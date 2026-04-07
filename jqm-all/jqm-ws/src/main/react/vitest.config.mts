import { defineConfig } from "vitest/config";

export default defineConfig({
    test: {
        globals: true,
        environment: "jsdom",
        setupFiles: [
            "./src/setupTests.ts",
        ],
        pool: "threads",
        coverage: {
            provider: "v8",
            reporter: ["text", "lcov"],
            include: ["src/components/**", "src/utils/**"],
            exclude: ["src/**/*.test.*", "src/test-utils/**"],
        },
    },
});
