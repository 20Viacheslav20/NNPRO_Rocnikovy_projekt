import { defineConfig, devices } from '@playwright/test';

/**
 * Playwright configuration for Ticket System E2E tests
 * 
 * Before running tests, make sure:
 * 1. Backend is running on http://localhost:8080
 * 2. Frontend is running on http://localhost:4200
 */
export default defineConfig({
  testDir: './tests',
  
  /* Run tests in files in parallel */
  fullyParallel: false,
  
  /* Fail the build on CI if you accidentally left test.only in the source code */
  forbidOnly: !!process.env.CI,
  
  /* Retry on CI only */
  retries: process.env.CI ? 2 : 0,
  
  /* Opt out of parallel tests - we need sequential execution for E2E flow */
  workers: 1,
  
  /* Reporter to use */
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['list']
  ],
  
  /* Shared settings for all the projects below */
  use: {
    /* Base URL for the frontend */
    baseURL: 'http://localhost:4200',

    /* Collect trace when retrying the failed test */
    trace: 'on-first-retry',
    
    /* Screenshot on failure */
    screenshot: 'only-on-failure',
    
    /* Video on failure */
    video: 'on-first-retry',
  },

  /* Configure projects for major browsers */
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
    // Uncomment to test in other browsers
    // {
    //   name: 'firefox',
    //   use: { ...devices['Desktop Firefox'] },
    // },
    // {
    //   name: 'webkit',
    //   use: { ...devices['Desktop Safari'] },
    // },
  ],

  /* Global timeout for each test */
  timeout: 60000,
  
  /* Timeout for each assertion */
  expect: {
    timeout: 10000
  },

  /* Run your local dev server before starting the tests */
  // Uncomment if you want Playwright to start servers automatically
  // webServer: [
  //   {
  //     command: 'cd ../backend/backend && ./mvnw spring-boot:run',
  //     url: 'http://localhost:8080/api/auth/login',
  //     reuseExistingServer: !process.env.CI,
  //     timeout: 120000,
  //   },
  //   {
  //     command: 'cd ../frontend && npm start',
  //     url: 'http://localhost:4200',
  //     reuseExistingServer: !process.env.CI,
  //     timeout: 120000,
  //   },
  // ],
});
