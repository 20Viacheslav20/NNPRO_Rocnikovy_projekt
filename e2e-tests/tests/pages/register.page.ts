import { Page, Locator, expect } from '@playwright/test';

/**
 * Page Object for Register page
 */
export class RegisterPage {
  readonly page: Page;
  readonly emailInput: Locator;
  readonly nameInput: Locator;
  readonly surnameInput: Locator;
  readonly passwordInput: Locator;
  readonly registerButton: Locator;
  readonly loginLink: Locator;
  readonly errorMessage: Locator;

  constructor(page: Page) {
    this.page = page;
    this.emailInput = page.locator('input[name="email"]');
    this.nameInput = page.locator('input[name="name"]');
    this.surnameInput = page.locator('input[name="surname"]');
    this.passwordInput = page.locator('input[name="password"]');
    this.registerButton = page.locator('button[type="submit"]');
    this.loginLink = page.locator('a[routerLink="/login"]');
    this.errorMessage = page.locator('.error');
  }

  async goto() {
    await this.page.goto('/register');
    await expect(this.page.locator('h2')).toContainText('Register');
  }

  async register(email: string, name: string, surname: string, password: string) {
    await this.emailInput.fill(email);
    await this.nameInput.fill(name);
    await this.surnameInput.fill(surname);
    await this.passwordInput.fill(password);
    await this.registerButton.click();
  }

  async expectError(message: string) {
    await expect(this.errorMessage).toContainText(message);
  }

  async goToLogin() {
    await this.loginLink.click();
    await expect(this.page.locator('h2')).toContainText('Login');
  }
}
