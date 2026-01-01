import { test, expect } from '@playwright/test';
import { LoginPage, RegisterPage, ProjectsPage, TicketsPage, NavigationPage } from './pages';

/**
 * E2E Flow Test for Ticket System
 *
 * Complete test coverage:
 * - User registration and authentication
 * - Project CRUD operations
 * - Ticket CRUD operations
 * - Ticket lifecycle (state changes)
 * - Ticket Comments
 * - Ticket History
 * - User management
 * - Navigation
 */

// ============================================
// TEST DATA
// ============================================
const timestamp = Date.now();
const testData = {
    user: {
        email: `e2e-test-${timestamp}@example.com`,
        name: 'E2E',
        surname: 'TestUser',
        password: 'Password123!'
    },
    project: {
        name: `E2E Test Project ${timestamp}`,
        description: 'Project created during E2E testing'
    },
    ticket: {
        name: 'E2E Test Bug',
        description: 'Bug found during E2E testing'
    }
};

let userRegistered = false;

// ============================================
// MAIN E2E FLOW: Complete User Journey
// ============================================
test.describe.serial('E2E Flow Test: Complete User Journey', () => {

    test('1. New user registration', async ({ page }) => {
        const registerPage = new RegisterPage(page);

        await registerPage.goto();
        await registerPage.register(
            testData.user.email,
            testData.user.name,
            testData.user.surname,
            testData.user.password
        );

        // Wait for API response and redirect
        await page.waitForTimeout(3000);

        // After registration, frontend redirects to /login
        await expect(page).toHaveURL(/\/login/, { timeout: 15000 });
        userRegistered = true;
    });

    test('2. User login after registration', async ({ page }) => {
        test.skip(!userRegistered, 'User was not registered');

        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(testData.user.email, testData.user.password);

        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.expectLoggedIn();
    });

    test('3. Create new project', async ({ page }) => {
        test.skip(!userRegistered, 'User was not registered');

        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(testData.user.email, testData.user.password);
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.goToProjects();

        const projectsPage = new ProjectsPage(page);
        await projectsPage.createProject(testData.project.name, testData.project.description);
        await projectsPage.expectProjectExists(testData.project.name);
    });

    test('4. Add ticket to project', async ({ page }) => {
        test.skip(!userRegistered, 'User was not registered');

        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(testData.user.email, testData.user.password);
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.goToProjects();

        const projectsPage = new ProjectsPage(page);
        await projectsPage.waitForLoad();
        await projectsPage.openProject(testData.project.name);

        const ticketsPage = new TicketsPage(page);
        await ticketsPage.waitForLoad();
        await ticketsPage.createTicket(
            testData.ticket.name,
            testData.ticket.description,
            'bug',
            'high'
        );

        await ticketsPage.expectTicketExists(testData.ticket.name);
    });

    test('5. Change ticket state: open -> in_progress -> done', async ({ page }) => {
        test.skip(!userRegistered, 'User was not registered');

        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(testData.user.email, testData.user.password);
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.goToProjects();

        const projectsPage = new ProjectsPage(page);
        await projectsPage.waitForLoad();
        await projectsPage.openProject(testData.project.name);

        const ticketsPage = new TicketsPage(page);
        await ticketsPage.waitForLoad();

        // open -> in_progress
        await ticketsPage.editTicket(testData.ticket.name, { state: 'in_progress' });
        await ticketsPage.expectTicketState(testData.ticket.name, 'In progress');

        // in_progress -> done
        await ticketsPage.editTicket(testData.ticket.name, { state: 'done' });
        await ticketsPage.expectTicketState(testData.ticket.name, 'Done');
    });

    test('6. Delete ticket', async ({ page }) => {
        test.skip(!userRegistered, 'User was not registered');

        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(testData.user.email, testData.user.password);
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.goToProjects();

        const projectsPage = new ProjectsPage(page);
        await projectsPage.waitForLoad();
        await projectsPage.openProject(testData.project.name);

        const ticketsPage = new TicketsPage(page);
        await ticketsPage.waitForLoad();
        await ticketsPage.deleteTicket(testData.ticket.name);
        await ticketsPage.expectTicketNotExists(testData.ticket.name);
    });

    test('7. Delete project', async ({ page }) => {
        test.skip(!userRegistered, 'User was not registered');

        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(testData.user.email, testData.user.password);
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.goToProjects();

        const projectsPage = new ProjectsPage(page);
        await projectsPage.waitForLoad();
        await projectsPage.deleteProject(testData.project.name);
        await projectsPage.expectProjectNotExists(testData.project.name);
    });

    test('8. User logout', async ({ page }) => {
        test.skip(!userRegistered, 'User was not registered');

        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(testData.user.email, testData.user.password);
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.logout();
        await expect(page).toHaveURL(/\/login/);
    });
});

// ============================================
// LOGIN VALIDATION
// ============================================
test('Login with invalid credentials shows error', async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();

    await loginPage.login('invalid@email.com', 'wrongpassword');

    await expect(page).toHaveURL(/\/login/);
    await page.waitForTimeout(2000);
    await expect(page.locator('.error')).toBeVisible({ timeout: 5000 });
});

// ============================================
// PROJECT CRUD OPERATIONS
// ============================================
test.describe('Project Management', () => {
    let testEmail: string;

    test.beforeAll(async ({ browser }) => {
        testEmail = `e2e-project-${Date.now()}@example.com`;
        const page = await browser.newPage();
        const registerPage = new RegisterPage(page);
        await registerPage.goto();
        await registerPage.register(testEmail, 'Project', 'Tester', 'Password123!');
        await page.waitForTimeout(2000);
        await page.waitForURL(/\/login/, { timeout: 15000 });
        await page.close();
    });

    test('Create project with name and description', async ({ page }) => {
        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(testEmail, 'Password123!');
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.goToProjects();

        const projectsPage = new ProjectsPage(page);
        const projectName = `Test Project ${Date.now()}`;

        await projectsPage.createProject(projectName, 'Test description');
        await projectsPage.expectProjectExists(projectName);

        // Cleanup
        await projectsPage.deleteProject(projectName);
    });
});

// ============================================
// TICKET CRUD OPERATIONS
// ============================================
test.describe('Ticket Management', () => {
    let testEmail: string;
    let projectName: string;

    test.beforeAll(async ({ browser }) => {
        testEmail = `e2e-ticket-${Date.now()}@example.com`;
        projectName = `Ticket Test Project ${Date.now()}`;

        const page = await browser.newPage();
        const registerPage = new RegisterPage(page);
        await registerPage.goto();
        await registerPage.register(testEmail, 'Ticket', 'Tester', 'Password123!');
        await page.waitForTimeout(2000);
        await page.waitForURL(/\/login/, { timeout: 15000 });

        // Login and create project
        const loginPage = new LoginPage(page);
        await loginPage.login(testEmail, 'Password123!');
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.goToProjects();

        const projectsPage = new ProjectsPage(page);
        await projectsPage.createProject(projectName, 'Project for ticket testing');
        await page.close();
    });

    test('Edit ticket details', async ({ page }) => {
        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(testEmail, 'Password123!');
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.goToProjects();

        const projectsPage = new ProjectsPage(page);
        await projectsPage.waitForLoad();
        await projectsPage.openProject(projectName);

        const ticketsPage = new TicketsPage(page);
        await ticketsPage.waitForLoad();

        // Create ticket
        await ticketsPage.createTicket('Edit Me', 'Original description', 'bug', 'low');
        await ticketsPage.expectTicketExists('Edit Me');

        // Edit multiple fields
        await ticketsPage.editTicket('Edit Me', {
            name: 'Edited Ticket',
            description: 'Updated description',
            priority: 'high',
            type: 'feature'
        });

        await ticketsPage.expectTicketExists('Edited Ticket');
        await ticketsPage.expectTicketNotExists('Edit Me');

        // Cleanup
        await ticketsPage.deleteTicket('Edited Ticket');
    });

    test('View ticket details', async ({ page }) => {
        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(testEmail, 'Password123!');
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.goToProjects();

        const projectsPage = new ProjectsPage(page);
        await projectsPage.waitForLoad();
        await projectsPage.openProject(projectName);

        const ticketsPage = new TicketsPage(page);
        await ticketsPage.waitForLoad();

        // Create ticket
        await ticketsPage.createTicket('View Me', 'Detailed description', 'task', 'med');

        // View ticket details
        await ticketsPage.viewTicket('View Me');

        // Verify on ticket detail page
        await expect(page).toHaveURL(/\/tickets\/[a-f0-9-]+/);
        await expect(page.locator('mat-card-subtitle')).toContainText('View Me');
        await expect(page.locator('.description p')).toContainText('Detailed description');

        // Go back and cleanup
        await page.locator('.back-link').click();
        await ticketsPage.waitForLoad();
        await ticketsPage.deleteTicket('View Me');
    });
});

// ============================================
// TICKET COMMENTS
// ============================================
test.describe('Ticket Comments', () => {
    let testEmail: string;
    let projectName: string;

    test.beforeAll(async ({ browser }) => {
        testEmail = `e2e-comments-${Date.now()}@example.com`;
        projectName = `Comments Test Project ${Date.now()}`;

        const page = await browser.newPage();
        const registerPage = new RegisterPage(page);
        await registerPage.goto();
        await registerPage.register(testEmail, 'Comments', 'Tester', 'Password123!');
        await page.waitForTimeout(2000);
        await page.waitForURL(/\/login/, { timeout: 15000 });

        const loginPage = new LoginPage(page);
        await loginPage.login(testEmail, 'Password123!');
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.goToProjects();

        const projectsPage = new ProjectsPage(page);
        await projectsPage.createProject(projectName, 'Project for comments testing');
        await page.close();
    });

    test('Add comment to ticket', async ({ page }) => {
        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(testEmail, 'Password123!');
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.goToProjects();

        const projectsPage = new ProjectsPage(page);
        await projectsPage.waitForLoad();
        await projectsPage.openProject(projectName);

        const ticketsPage = new TicketsPage(page);
        await ticketsPage.waitForLoad();

        // Create ticket
        await ticketsPage.createTicket('Comment Test Ticket', 'Test description', 'task', 'med');

        // View ticket details
        await ticketsPage.viewTicket('Comment Test Ticket');
        await expect(page).toHaveURL(/\/tickets\/[a-f0-9-]+/);

        // Wait for comments section to load
        await expect(page.locator('.comments-card h3')).toContainText('Comments');

        // Add a comment
        const newCommentTextarea = page.locator('.new-comment textarea');
        await newCommentTextarea.fill('This is my first comment');
        await page.locator('button:has-text("Add comment")').click();

        // Verify comment appears
        await page.waitForTimeout(2000);
        await expect(page.locator('.comment-text').first()).toContainText('This is my first comment');

        // Go back and cleanup
        await page.locator('.back-link').click();
        await ticketsPage.waitForLoad();
        await ticketsPage.deleteTicket('Comment Test Ticket');
    });

    test('Edit comment', async ({ page }) => {
        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(testEmail, 'Password123!');
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.goToProjects();

        const projectsPage = new ProjectsPage(page);
        await projectsPage.waitForLoad();
        await projectsPage.openProject(projectName);

        const ticketsPage = new TicketsPage(page);
        await ticketsPage.waitForLoad();

        // Create ticket
        await ticketsPage.createTicket('Edit Comment Ticket', 'Test description', 'bug', 'high');

        // View ticket details
        await ticketsPage.viewTicket('Edit Comment Ticket');

        // Add a comment
        const newCommentTextarea = page.locator('.new-comment textarea');
        await newCommentTextarea.fill('Original comment');
        await page.locator('button:has-text("Add comment")').click();
        await page.waitForTimeout(2000);

        // Click edit button on comment
        await page.locator('.comment-actions button mat-icon:has-text("edit")').click();

        // Edit the comment - find textarea in edit mode
        await page.waitForTimeout(500);
        const editTextarea = page.locator('.comment mat-form-field textarea');
        await editTextarea.clear();
        await editTextarea.fill('Updated comment text');
        await page.locator('.actions button:has-text("Save")').click();

        // Verify updated comment
        await page.waitForTimeout(2000);
        await expect(page.locator('.comment-text').first()).toContainText('Updated comment text');

        // Cleanup
        await page.locator('.back-link').click();
        await ticketsPage.waitForLoad();
        await ticketsPage.deleteTicket('Edit Comment Ticket');
    });

    test('Delete comment', async ({ page }) => {
        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(testEmail, 'Password123!');
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.goToProjects();

        const projectsPage = new ProjectsPage(page);
        await projectsPage.waitForLoad();
        await projectsPage.openProject(projectName);

        const ticketsPage = new TicketsPage(page);
        await ticketsPage.waitForLoad();

        // Create ticket
        await ticketsPage.createTicket('Delete Comment Ticket', 'Test description', 'feature', 'low');

        // View ticket details
        await ticketsPage.viewTicket('Delete Comment Ticket');

        // Add a comment
        const newCommentTextarea = page.locator('.new-comment textarea');
        await newCommentTextarea.fill('Comment to delete');
        await page.locator('button:has-text("Add comment")').click();
        await page.waitForTimeout(2000);

        // Verify comment exists
        await expect(page.locator('.comment-text').first()).toContainText('Comment to delete');

        // Get initial count
        const initialCount = await page.locator('.comment').count();

        // Delete comment - set up dialog handler before clicking
        page.once('dialog', dialog => dialog.accept());
        await page.locator('.comment-actions button mat-icon:has-text("delete")').click();
        await page.waitForTimeout(2000);

        // Verify comment count decreased
        const newCount = await page.locator('.comment').count();
        expect(newCount).toBeLessThan(initialCount);

        // Cleanup
        await page.locator('.back-link').click();
        await ticketsPage.waitForLoad();
        await ticketsPage.deleteTicket('Delete Comment Ticket');
    });
});

// ============================================
// TICKET HISTORY
// ============================================
test.describe('Ticket History', () => {
    let testEmail: string;
    let projectName: string;

    test.beforeAll(async ({ browser }) => {
        testEmail = `e2e-history-${Date.now()}@example.com`;
        projectName = `History Test Project ${Date.now()}`;

        const page = await browser.newPage();
        const registerPage = new RegisterPage(page);
        await registerPage.goto();
        await registerPage.register(testEmail, 'History', 'Tester', 'Password123!');
        await page.waitForTimeout(2000);
        await page.waitForURL(/\/login/, { timeout: 15000 });

        const loginPage = new LoginPage(page);
        await loginPage.login(testEmail, 'Password123!');
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.goToProjects();

        const projectsPage = new ProjectsPage(page);
        await projectsPage.createProject(projectName, 'Project for history testing');
        await page.close();
    });

    test('View ticket creation in history', async ({ page }) => {
        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(testEmail, 'Password123!');
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.goToProjects();

        const projectsPage = new ProjectsPage(page);
        await projectsPage.waitForLoad();
        await projectsPage.openProject(projectName);

        const ticketsPage = new TicketsPage(page);
        await ticketsPage.waitForLoad();

        // Create ticket
        await ticketsPage.createTicket('History Test Ticket', 'Test description', 'task', 'med');

        // View ticket details
        await ticketsPage.viewTicket('History Test Ticket');
        await expect(page).toHaveURL(/\/tickets\/[a-f0-9-]+/);

        // Wait for history section to load
        await expect(page.locator('mat-card-title:has-text("Ticket History")')).toBeVisible();

        // Verify CREATED action in history
        await expect(page.locator('.history-item .action').first()).toContainText('CREATED');

        // Cleanup
        await page.locator('.back-link').click();
        await ticketsPage.waitForLoad();
        await ticketsPage.deleteTicket('History Test Ticket');
    });

    test('View multiple field changes in history', async ({ page }) => {
        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(testEmail, 'Password123!');
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.goToProjects();

        const projectsPage = new ProjectsPage(page);
        await projectsPage.waitForLoad();
        await projectsPage.openProject(projectName);

        const ticketsPage = new TicketsPage(page);
        await ticketsPage.waitForLoad();

        // Create ticket
        await ticketsPage.createTicket('Multi Edit Ticket', 'Original description', 'task', 'low');

        // Edit multiple fields at once
        await ticketsPage.editTicket('Multi Edit Ticket', {
            name: 'Renamed Ticket',
            description: 'New description',
            priority: 'high'
        });

        // View ticket details
        await ticketsPage.viewTicket('Renamed Ticket');
        await expect(page).toHaveURL(/\/tickets\/[a-f0-9-]+/);

        // Wait for history to load
        await page.waitForTimeout(1000);

        // Verify history contains multiple field changes
        const historyItems = page.locator('.history-item');
        const historyCount = await historyItems.count();
        expect(historyCount).toBeGreaterThanOrEqual(2); // At least CREATED + some updates

        // Cleanup
        await page.locator('.back-link').click();
        await ticketsPage.waitForLoad();
        await ticketsPage.deleteTicket('Renamed Ticket');
    });
});

// ============================================
// USER MANAGEMENT
// ============================================
test.describe('User Management', () => {
    let adminEmail: string;

    test.beforeAll(async ({ browser }) => {
        adminEmail = `e2e-admin-${Date.now()}@example.com`;
        const page = await browser.newPage();
        const registerPage = new RegisterPage(page);
        await registerPage.goto();
        await registerPage.register(adminEmail, 'Admin', 'User', 'Password123!');
        await page.waitForTimeout(2000);
        await page.waitForURL(/\/login/, { timeout: 15000 });
        await page.close();
    });

    test('View users list', async ({ page }) => {
        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(adminEmail, 'Password123!');
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.goToUsers();

        // Verify on users page
        await expect(page).toHaveURL('/users');

        // Should see user table with at least 1 row
        await expect(page.locator('table.mat-mdc-table')).toBeVisible();
        // Wait for data rows to appear (Angular Material uses mat-mdc-row class)
        await page.locator('tr.mat-mdc-row').first().waitFor({ state: 'visible', timeout: 10000 });
        const rowCount = await page.locator('tr.mat-mdc-row').count();
        expect(rowCount).toBeGreaterThanOrEqual(1);
    });

    test('Create new user via dialog', async ({ page }) => {
        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(adminEmail, 'Password123!');
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.goToUsers();

        // Click create button
        await page.locator('button:has-text("Create")').click();

        // Wait for dialog
        const dialog = page.locator('mat-dialog-container');
        await expect(dialog).toBeVisible();

        const newUserEmail = `new-user-${Date.now()}@example.com`;

        // Fill form fields one by one with waits
        await page.locator('input[formcontrolname="email"]').fill(newUserEmail);
        await page.waitForTimeout(100);

        await page.locator('input[formcontrolname="name"]').fill('New');
        await page.waitForTimeout(100);

        await page.locator('input[formcontrolname="surname"]').fill('User');
        await page.waitForTimeout(100);

        await page.locator('input[formcontrolname="password"]').fill('Password123!');
        await page.waitForTimeout(100);

        // Select role - click to open dropdown
        await page.locator('mat-select[formcontrolname="role"]').click();
        await page.waitForTimeout(300);
        await page.locator('mat-option:has-text("User")').click();
        await page.waitForTimeout(300);

        // Wait for form to be valid
        await page.waitForTimeout(500);

        // Click Create button (wait for it to be enabled)
        const createBtn = page.locator('mat-dialog-container button:has-text("Create")');
        await expect(createBtn).toBeEnabled({ timeout: 5000 });
        await createBtn.click();

        // Wait for dialog to close
        await expect(dialog).toBeHidden({ timeout: 15000 });
    });
});

// ============================================
// NAVIGATION & AUTH GUARDS
// ============================================
test.describe('Navigation and Security', () => {
    test('Unauthenticated user redirected to login', async ({ page }) => {
        // Try to access protected routes without login
        await page.goto('/projects');
        await expect(page).toHaveURL(/\/login/);

        await page.goto('/users');
        await expect(page).toHaveURL(/\/login/);

        await page.goto('/tickets');
        await expect(page).toHaveURL(/\/login/);
    });

    test('Navigate between pages', async ({ page }) => {
        const testEmail = `e2e-nav-${Date.now()}@example.com`;

        // Register
        const registerPage = new RegisterPage(page);
        await registerPage.goto();
        await registerPage.register(testEmail, 'Nav', 'User', 'Password123!');
        await page.waitForTimeout(2000);
        await expect(page).toHaveURL(/\/login/, { timeout: 15000 });

        // Login
        const loginPage = new LoginPage(page);
        await loginPage.login(testEmail, 'Password123!');
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);

        // Navigate to projects
        await nav.goToProjects();
        await expect(page).toHaveURL('/projects');

        // Navigate to users
        await nav.goToUsers();
        await expect(page).toHaveURL('/users');

        // Navigate back to projects via logo/title
        await nav.goToProjects();
        await expect(page).toHaveURL('/projects');

        // Logout
        await nav.logout();
        await expect(page).toHaveURL(/\/login/);
    });

    test('Session persistence after page reload', async ({ page }) => {
        const testEmail = `e2e-session-${Date.now()}@example.com`;

        // Register
        const registerPage = new RegisterPage(page);
        await registerPage.goto();
        await registerPage.register(testEmail, 'Session', 'User', 'Password123!');
        await page.waitForTimeout(2000);
        await expect(page).toHaveURL(/\/login/, { timeout: 15000 });

        // Login
        const loginPage = new LoginPage(page);
        await loginPage.login(testEmail, 'Password123!');
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        // Reload page
        await page.reload();

        // Should still be logged in (on projects or tickets page)
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.expectLoggedIn();
    });
});