import { Page, Locator, expect } from '@playwright/test';

/**
 * Page Object for Users page
 */
export class UsersPage {
  readonly page: Page;
  readonly createButton: Locator;
  readonly usersTable: Locator;
  readonly progressBar: Locator;

  constructor(page: Page) {
    this.page = page;
    this.createButton = page.locator('button:has-text("Create")');
    this.usersTable = page.locator('table.mat-mdc-table');
    this.progressBar = page.locator('mat-progress-bar');
  }

  async goto() {
    await this.page.goto('/users');
    await this.waitForLoad();
  }

  async waitForLoad() {
    await this.progressBar.waitFor({ state: 'hidden', timeout: 10000 }).catch(() => {});
    await this.page.waitForTimeout(500);
  }

  async createUser(email: string, name: string, surname: string, password: string, role: string = 'USER') {
    await this.createButton.click();
    
    const dialog = this.page.locator('mat-dialog-container');
    await expect(dialog).toBeVisible();
    
    await this.page.locator('input[formcontrolname="email"]').fill(email);
    await this.page.locator('input[formcontrolname="name"]').fill(name);
    await this.page.locator('input[formcontrolname="surname"]').fill(surname);
    await this.page.locator('input[formcontrolname="password"]').fill(password);
    
    // Select role if dropdown exists
    const roleSelect = this.page.locator('mat-select[formcontrolname="role"]');
    if (await roleSelect.isVisible()) {
      await roleSelect.click();
      await this.page.locator(`mat-option:has-text("${role}")`).click();
    }
    
    await this.page.locator('mat-dialog-container button:has-text("Save")').click();
    await expect(dialog).toBeHidden({ timeout: 10000 });
    
    await this.waitForLoad();
  }

  async findUserRow(identifier: string): Promise<Locator> {
    return this.page.locator(`tr.data-row:has-text("${identifier}")`);
  }

  async editUser(identifier: string, updates: { name?: string; surname?: string; role?: string }) {
    const row = await this.findUserRow(identifier);
    await row.locator('button[mattooltip="Edit"]').click();
    
    const dialog = this.page.locator('mat-dialog-container');
    await expect(dialog).toBeVisible();
    
    if (updates.name) {
      const nameInput = this.page.locator('input[formcontrolname="name"]');
      await nameInput.clear();
      await nameInput.fill(updates.name);
    }
    
    if (updates.surname) {
      const surnameInput = this.page.locator('input[formcontrolname="surname"]');
      await surnameInput.clear();
      await surnameInput.fill(updates.surname);
    }
    
    if (updates.role) {
      await this.page.locator('mat-select[formcontrolname="role"]').click();
      await this.page.locator(`mat-option:has-text("${updates.role}")`).click();
    }
    
    await this.page.locator('mat-dialog-container button:has-text("Save")').click();
    await expect(dialog).toBeHidden({ timeout: 10000 });
    
    await this.waitForLoad();
  }

  async deleteUser(identifier: string) {
    const row = await this.findUserRow(identifier);
    
    this.page.once('dialog', dialog => dialog.accept());
    await row.locator('button[mattooltip="Delete"]').click();
    
    await this.waitForLoad();
  }

  async expectUserExists(identifier: string) {
    const row = await this.findUserRow(identifier);
    await expect(row).toBeVisible();
  }

  async expectUserNotExists(identifier: string) {
    const row = await this.findUserRow(identifier);
    await expect(row).toBeHidden();
  }

  async getUserCount(): Promise<number> {
    const rows = this.page.locator('tr.data-row');
    return await rows.count();
  }
}
