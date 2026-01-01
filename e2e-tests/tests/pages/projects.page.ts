import { Page, Locator, expect } from '@playwright/test';

/**
 * Page Object for Projects page
 */
export class ProjectsPage {
    readonly page: Page;
    readonly createButton: Locator;
    readonly projectsTable: Locator;
    readonly searchInput: Locator;
    readonly statusFilter: Locator;
    readonly progressBar: Locator;

    constructor(page: Page) {
        this.page = page;
        this.createButton = page.locator('button:has-text("Create")');
        this.projectsTable = page.locator('table.mat-mdc-table');
        this.searchInput = page.locator('input[placeholder="Project name"]');
        this.statusFilter = page.locator('mat-select').first();
        this.progressBar = page.locator('mat-progress-bar');
    }

    async goto() {
        await this.page.goto('/projects');
        await this.waitForLoad();
    }

    async waitForLoad() {
        await this.progressBar.waitFor({ state: 'hidden', timeout: 10000 }).catch(() => { });
        await this.page.waitForTimeout(500);
    }

    async createProject(name: string, description: string = '') {
        await this.createButton.click();

        const dialog = this.page.locator('mat-dialog-container');
        await expect(dialog).toBeVisible();

        await this.page.locator('input[formcontrolname="name"]').fill(name);
        await this.page.locator('textarea[formcontrolname="description"]').fill(description);

        // In create mode button says "Create"
        await this.page.locator('mat-dialog-container button:has-text("Create")').click();

        // Wait for dialog to close
        await expect(dialog).toBeHidden({ timeout: 10000 });

        // Wait for table to reload
        await this.page.waitForTimeout(1000);
        await this.waitForLoad();
    }

    async findProjectRow(name: string): Promise<Locator> {
        return this.page.locator(`tr.data-row:has-text("${name}")`);
    }

    async openProject(name: string) {
        const row = await this.findProjectRow(name);
        await row.locator('button[mattooltip="Open project"]').click();
        await this.page.waitForURL(/\/projects\/.*\/tickets/);
    }

    async editProject(name: string, newName: string, newDescription?: string) {
        const row = await this.findProjectRow(name);
        await row.locator('button[mattooltip="Edit"]').click();

        const dialog = this.page.locator('mat-dialog-container');
        await expect(dialog).toBeVisible();

        // Wait for form to be ready
        await this.page.waitForTimeout(500);

        // Fill name
        const nameInput = this.page.locator('input[formcontrolname="name"]');
        await nameInput.clear();
        await nameInput.fill(newName);

        // Fill description if provided
        if (newDescription !== undefined) {
            const descInput = this.page.locator('textarea[formcontrolname="description"]');
            await descInput.clear();
            await descInput.fill(newDescription);
        }

        // In edit mode, status field is shown - it should already have a value
        // but we need to make sure it's properly set
        const statusSelect = this.page.locator('mat-select[formcontrolname="status"]');
        if (await statusSelect.isVisible()) {
            await statusSelect.click();
            // Wait for dropdown to open
            await this.page.waitForTimeout(300);
            // Select Active option
            await this.page.locator('mat-option:has-text("Active")').click();
            // Wait for dropdown to close
            await this.page.waitForTimeout(500);
        }

        // Wait a moment for form validation
        await this.page.waitForTimeout(300);

        // In edit mode button says "Save"
        const saveButton = this.page.locator('mat-dialog-container button:has-text("Save")');

        // Wait for button to be enabled
        await expect(saveButton).toBeEnabled({ timeout: 5000 });

        // Click save
        await saveButton.click();

        // Wait for dialog to close (API call + close animation)
        await expect(dialog).toBeHidden({ timeout: 15000 });

        await this.page.waitForTimeout(1000);
        await this.waitForLoad();
    }

    // Simple edit that only changes name - status should already be set from backend
    async editProjectSimple(name: string, newName: string) {
        const row = await this.findProjectRow(name);
        await row.locator('button[mattooltip="Edit"]').click();

        const dialog = this.page.locator('mat-dialog-container');
        await expect(dialog).toBeVisible();

        // Wait for form to be ready and populated from backend
        await this.page.waitForTimeout(1000);

        // Fill name
        const nameInput = this.page.locator('input[formcontrolname="name"]');
        await nameInput.clear();
        await nameInput.fill(newName);

        // Wait a moment for form validation
        await this.page.waitForTimeout(500);

        // In edit mode button says "Save"
        const saveButton = this.page.locator('mat-dialog-container button:has-text("Save")');

        // Wait for button to be enabled
        await expect(saveButton).toBeEnabled({ timeout: 10000 });

        // Click save
        await saveButton.click();

        // Wait for dialog to close (API call + close animation)
        await expect(dialog).toBeHidden({ timeout: 15000 });

        await this.page.waitForTimeout(1000);
        await this.waitForLoad();
    }

    async deleteProject(name: string) {
        const row = await this.findProjectRow(name);

        // Set up dialog handler before clicking
        this.page.once('dialog', dialog => dialog.accept());
        await row.locator('button[mattooltip="Delete"]').click();

        await this.page.waitForTimeout(1000);
        await this.waitForLoad();
    }

    async searchProject(searchText: string) {
        await this.searchInput.fill(searchText);
        await this.page.waitForTimeout(500);
    }

    async filterByStatus(status: 'ACTIVE' | 'ARCHIVED' | '') {
        await this.statusFilter.click();
        if (status === '') {
            await this.page.locator('mat-option:has-text("All")').click();
        } else {
            await this.page.locator(`mat-option:has-text("${status}")`).click();
        }
        await this.page.waitForTimeout(500);
    }

    async expectProjectExists(name: string) {
        const row = await this.findProjectRow(name);
        await expect(row).toBeVisible({ timeout: 5000 });
    }

    async expectProjectNotExists(name: string) {
        const row = await this.findProjectRow(name);
        await expect(row).toBeHidden({ timeout: 5000 });
    }

    async getProjectCount(): Promise<number> {
        const rows = this.page.locator('tr.data-row');
        return await rows.count();
    }
}