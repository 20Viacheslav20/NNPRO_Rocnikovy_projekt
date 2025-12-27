import { Page, Locator, expect } from '@playwright/test';

/**
 * Page Object for Navigation (toolbar)
 */
export class NavigationPage {
  readonly page: Page;
  readonly toolbar: Locator;
  readonly homeLink: Locator;
  readonly usersButton: Locator;
  readonly userMenuButton: Locator;
  readonly logoutButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.toolbar = page.locator('mat-toolbar');
    this.homeLink = page.locator('a.title-link');
    // Users button has mat-icon with "admin_panel_settings"
    this.usersButton = page.locator('button:has(mat-icon:has-text("admin_panel_settings"))');
    this.userMenuButton = page.locator('button.user-button');
    this.logoutButton = page.locator('button:has-text("Logout")');
  }

  async goToProjects() {
    await this.homeLink.click();
    await expect(this.page).toHaveURL('/projects');
  }

  async goToUsers() {
    // Check if Users button is visible (only for admins)
    if (await this.usersButton.isVisible()) {
      await this.usersButton.click();
      await expect(this.page).toHaveURL('/users');
    } else {
      // Navigate directly
      await this.page.goto('/users');
      await expect(this.page).toHaveURL('/users');
    }
  }

  async logout() {
    await this.userMenuButton.click();
    await this.page.waitForTimeout(300);
    await this.logoutButton.click();
    await expect(this.page).toHaveURL('/login');
  }

  async expectLoggedIn() {
    await expect(this.userMenuButton).toBeVisible({ timeout: 5000 });
  }

  async expectLoggedOut() {
    await expect(this.userMenuButton).toBeHidden();
  }

  async getUserName(): Promise<string> {
    await this.userMenuButton.click();
    await this.page.waitForTimeout(300);
    const nameElement = this.page.locator('mat-menu button:has(mat-icon:has-text("account_circle")) span');
    const name = await nameElement.textContent();
    // Close menu
    await this.page.keyboard.press('Escape');
    return name?.trim() || '';
  }
}
