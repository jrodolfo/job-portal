import { defineConfig } from '@playwright/test';

export default defineConfig({
    testDir: './tests/e2e',
    timeout: 30_000,
    fullyParallel: false,
    use: {
        baseURL: 'http://localhost:5173',
        headless: true,
        trace: 'on-first-retry'
    }
});
