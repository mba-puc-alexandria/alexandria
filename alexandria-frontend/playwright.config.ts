import { defineConfig, devices } from '@playwright/test';

const port = Number(process.env.E2E_FRONTEND_PORT || 3100);
const externalServer = process.env.E2E_BASE_URL;

export default defineConfig({
  globalSetup: './e2e/global-setup.ts',
  testDir: './e2e',
  testIgnore: process.env.RUN_RECOMMENDATION_PERFORMANCE ? [] : ['**/*performance.spec.ts'],
  fullyParallel: false,
  workers: 1,
  timeout: 30_000,
  expect: { timeout: 10_000 },
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    baseURL: externalServer || `http://localhost:${port}`,
    trace: 'retain-on-failure',
    ...devices['Desktop Chrome'],
  },
  webServer: externalServer ? undefined : {
    command: `npm run dev -- --port ${port}`,
    url: `http://localhost:${port}`,
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
    env: { NEXT_PUBLIC_API_URL: process.env.E2E_API_URL || 'http://localhost:18080' },
  },
});
