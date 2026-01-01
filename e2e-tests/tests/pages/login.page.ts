import { Page, Locator, expect } from '@playwright/test';

/**
 * Page Object for Login page
 */
export class LoginPage {
  readonly page: Page;
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly loginButton: Locator;
  readonly registerLink: Locator;
  readonly errorMessage: Locator;

  constructor(page: Page) {
    this.page = page;
    this.emailInput = page.locator('input[name="email"]');
    this.passwordInput = page.locator('input[name="password"]');
    this.loginButton = page.locator('button[type="submit"]');
    this.registerLink = page.locator('a[routerLink="/register"]');
    this.errorMessage = page.locator('.error');
  }

  async goto() {
    await this.page.goto('/login');
    await expect(this.page.locator('h2')).toContainText('Login');
  }

  async login(email: string, password: string) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.loginButton.click();
  }

  async expectError(message: string) {
    await expect(this.errorMessage).toContainText(message);
  }

  async goToRegister() {
    await this.registerLink.click();
    await expect(this.page.locator('h2')).toContainText('Register');
  }
}
