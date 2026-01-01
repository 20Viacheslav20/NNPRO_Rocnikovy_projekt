import { Page, Locator, expect } from '@playwright/test';
import { TicketType, TicketPriority, TicketState } from '../fixtures/test-data';

/**
 * Page Object for Tickets page
 */
export class TicketsPage {
  readonly page: Page;
  readonly createButton: Locator;
  readonly searchInput: Locator;
  readonly typeFilter: Locator;
  readonly priorityFilter: Locator;
  readonly stateFilter: Locator;
  readonly ticketsTable: Locator;
  readonly backLink: Locator;

  constructor(page: Page) {
    this.page = page;
    this.createButton = page.locator('button:has-text("New ticket")');
    this.searchInput = page.locator('input[placeholder="Name"]');
    this.typeFilter = page.locator('mat-select').nth(0);
    this.priorityFilter = page.locator('mat-select').nth(1);
    this.stateFilter = page.locator('mat-select').nth(2);
    this.ticketsTable = page.locator('table.mat-mdc-table');
    this.backLink = page.locator('a.back-link');
  }

  async waitForLoad() {
    await this.page.waitForTimeout(1000);
  }

  async createTicket(
    name: string, 
    description: string, 
    type: TicketType = 'bug', 
    priority: TicketPriority = 'med'
  ) {
    await this.createButton.click();
    
    // Wait for dialog
    const dialog = this.page.locator('mat-dialog-container');
    await expect(dialog).toBeVisible();
    
    // Fill form
    await this.page.locator('input[formcontrolname="name"]').fill(name);
    await this.page.locator('textarea[formcontrolname="description"]').fill(description);
    
    // Select type
    await this.page.locator('mat-select[formcontrolname="type"]').click();
    await this.page.locator(`mat-option[value="${type}"]`).click();
    
    // Select priority
    await this.page.locator('mat-select[formcontrolname="priority"]').click();
    await this.page.locator(`mat-option[value="${priority}"]`).click();
    
    // Click Save
    await this.page.locator('mat-dialog-container button:has-text("Save")').click();
    
    // Wait for dialog to close
    await expect(dialog).toBeHidden({ timeout: 10000 });
    
    await this.waitForLoad();
  }

  async findTicketRow(ticketName: string): Promise<Locator> {
    return this.page.locator(`tr.data-row:has-text("${ticketName}")`);
  }

  async viewTicket(ticketName: string) {
    const row = await this.findTicketRow(ticketName);
    await row.locator('button[title="View"]').click();
    
    // Wait for navigation to ticket detail
    await expect(this.page).toHaveURL(/\/tickets\//);
  }

  async editTicket(
    ticketName: string, 
    updates: {
      name?: string;
      description?: string;
      type?: TicketType;
      priority?: TicketPriority;
      state?: TicketState;
    }
  ) {
    const row = await this.findTicketRow(ticketName);
    await row.locator('button[title="Edit"]').click();
    
    // Wait for dialog
    const dialog = this.page.locator('mat-dialog-container');
    await expect(dialog).toBeVisible();
    
    // Update fields if provided
    if (updates.name) {
      const nameInput = this.page.locator('input[formcontrolname="name"]');
      await nameInput.clear();
      await nameInput.fill(updates.name);
    }
    
    if (updates.description) {
      const descInput = this.page.locator('textarea[formcontrolname="description"]');
      await descInput.clear();
      await descInput.fill(updates.description);
    }
    
    if (updates.type) {
      await this.page.locator('mat-select[formcontrolname="type"]').click();
      await this.page.locator(`mat-option[value="${updates.type}"]`).click();
    }
    
    if (updates.priority) {
      await this.page.locator('mat-select[formcontrolname="priority"]').click();
      await this.page.locator(`mat-option[value="${updates.priority}"]`).click();
    }
    
    if (updates.state) {
      await this.page.locator('mat-select[formcontrolname="state"]').click();
      await this.page.locator(`mat-option[value="${updates.state}"]`).click();
    }
    
    // Click Save
    await this.page.locator('mat-dialog-container button:has-text("Save")').click();
    
    // Wait for dialog to close
    await expect(dialog).toBeHidden({ timeout: 10000 });
    
    await this.waitForLoad();
  }

  async deleteTicket(ticketName: string) {
    const row = await this.findTicketRow(ticketName);
    
    // Setup dialog handler before clicking delete
    this.page.once('dialog', dialog => dialog.accept());
    
    await row.locator('button[title="Delete"]').click();
    
    await this.waitForLoad();
  }

  async expectTicketExists(ticketName: string) {
    const row = await this.findTicketRow(ticketName);
    await expect(row).toBeVisible();
  }

  async expectTicketNotExists(ticketName: string) {
    const row = await this.findTicketRow(ticketName);
    await expect(row).toBeHidden();
  }

  async expectTicketState(ticketName: string, state: string) {
    const row = await this.findTicketRow(ticketName);
    await expect(row).toContainText(state, { ignoreCase: true });
  }

  async getTicketCount(): Promise<number> {
    const rows = this.page.locator('tr.data-row');
    return await rows.count();
  }

  async searchTicket(name: string) {
    await this.searchInput.fill(name);
    await this.page.waitForTimeout(300);
  }

  async filterByType(type: 'All' | 'Bug' | 'Feature' | 'Task') {
    await this.page.locator('mat-form-field:has-text("Type") mat-select').click();
    await this.page.locator(`mat-option:has-text("${type}")`).click();
    await this.page.waitForTimeout(300);
  }

  async filterByState(state: 'All' | 'Open' | 'In progress' | 'Done') {
    await this.page.locator('mat-form-field:has-text("State") mat-select').click();
    await this.page.locator(`mat-option:has-text("${state}")`).click();
    await this.page.waitForTimeout(300);
  }
}
