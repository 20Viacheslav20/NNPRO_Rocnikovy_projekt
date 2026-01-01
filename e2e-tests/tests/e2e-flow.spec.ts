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
 * - User management
 * - Filtering and search
 * - Navigation
 *
 * Prerequisites:
 * - Backend running on http://localhost:8080
 * - Frontend running on http://localhost:4200
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

        // After registration, frontend redirects to /login
        await expect(page).toHaveURL('/login', { timeout: 15000 });
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
        await expect(page).toHaveURL('/login');
    });
});

// ============================================
// STANDALONE: Full ticket lifecycle in one test
// ============================================
test('Full ticket lifecycle in single test', async ({ page }) => {
    const uniqueEmail = `e2e-lifecycle-${Date.now()}@example.com`;
    const projectName = `Lifecycle Project ${Date.now()}`;
    const ticketName = 'Lifecycle Ticket';

    // 1. Register
    const registerPage = new RegisterPage(page);
    await registerPage.goto();
    await registerPage.register(uniqueEmail, 'Lifecycle', 'User', 'Password123!');
    await expect(page).toHaveURL('/login', { timeout: 15000 });

    // 2. Login
    const loginPage = new LoginPage(page);
    await loginPage.login(uniqueEmail, 'Password123!');
    await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

    const nav = new NavigationPage(page);
    const projectsPage = new ProjectsPage(page);
    const ticketsPage = new TicketsPage(page);

    // 3. Create project
    await nav.goToProjects();
    await projectsPage.createProject(projectName, 'Lifecycle test project');
    await projectsPage.expectProjectExists(projectName);

    // 4. Create ticket
    await projectsPage.openProject(projectName);
    await ticketsPage.waitForLoad();
    await ticketsPage.createTicket(ticketName, 'Lifecycle test ticket', 'task', 'med');
    await ticketsPage.expectTicketExists(ticketName);

    // 5. Change state: open -> in_progress -> done
    await ticketsPage.editTicket(ticketName, { state: 'in_progress' });
    await ticketsPage.expectTicketState(ticketName, 'In progress');

    await ticketsPage.editTicket(ticketName, { state: 'done' });
    await ticketsPage.expectTicketState(ticketName, 'Done');

    // 6. Delete ticket
    await ticketsPage.deleteTicket(ticketName);
    await ticketsPage.expectTicketNotExists(ticketName);

    // 7. Delete project
    await nav.goToProjects();
    await projectsPage.waitForLoad();
    await projectsPage.deleteProject(projectName);
    await projectsPage.expectProjectNotExists(projectName);

    // 8. Logout
    await nav.logout();
    await expect(page).toHaveURL('/login');
});

// ============================================
// LOGIN VALIDATION
// ============================================
test('Login with invalid credentials shows error', async ({ page }) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();

    await loginPage.login('invalid@email.com', 'wrongpassword');

    await expect(page).toHaveURL('/login');
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
        await page.waitForURL('/login', { timeout: 15000 });
        await page.close();
    });

    test('Create multiple projects', async ({ page }) => {
        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(testEmail, 'Password123!');
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.goToProjects();

        const projectsPage = new ProjectsPage(page);
        const project1 = `Multi Project 1 - ${Date.now()}`;
        const project2 = `Multi Project 2 - ${Date.now()}`;

        // Create first project and wait
        await projectsPage.createProject(project1, 'First project');
        await projectsPage.expectProjectExists(project1);

        // Wait a bit before creating second project
        await page.waitForTimeout(1000);

        // Create second project
        await projectsPage.createProject(project2, 'Second project');
        await projectsPage.expectProjectExists(project2);

        // Cleanup
        await projectsPage.deleteProject(project1);
        await projectsPage.deleteProject(project2);
    });

    test('Search projects by name', async ({ page }) => {
        const loginPage = new LoginPage(page);
        await loginPage.goto();
        await loginPage.login(testEmail, 'Password123!');
        await expect(page).toHaveURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.goToProjects();

        const projectsPage = new ProjectsPage(page);
        const uniqueName = `SearchTest-${Date.now()}`;

        // Create project with unique name
        await projectsPage.createProject(uniqueName, 'Search test');
        await projectsPage.expectProjectExists(uniqueName);

        // Search for it
        await projectsPage.searchProject(uniqueName);
        await page.waitForTimeout(500);

        // Should still be visible after search
        await projectsPage.expectProjectExists(uniqueName);

        // Cleanup
        await projectsPage.searchProject(''); // Clear search
        await page.waitForTimeout(500);
        await projectsPage.deleteProject(uniqueName);
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

        // Register
        const registerPage = new RegisterPage(page);
        await registerPage.goto();
        await registerPage.register(testEmail, 'Ticket', 'Tester', 'Password123!');
        await page.waitForURL('/login', { timeout: 15000 });

        // Login and create project
        const loginPage = new LoginPage(page);
        await loginPage.login(testEmail, 'Password123!');
        await page.waitForURL(/\/projects|\/tickets/, { timeout: 15000 });

        const nav = new NavigationPage(page);
        await nav.goToProjects();

        const projectsPage = new ProjectsPage(page);
        await projectsPage.createProject(projectName, 'For ticket tests');

        await page.close();
    });

    test('Create tickets of different types', async ({ page }) => {
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

        // Create bug
        await ticketsPage.createTicket('Bug Ticket', 'A bug', 'bug', 'high');
        await ticketsPage.expectTicketExists('Bug Ticket');

        // Create feature
        await ticketsPage.createTicket('Feature Ticket', 'A feature', 'feature', 'med');
        await ticketsPage.expectTicketExists('Feature Ticket');

        // Create task
        await ticketsPage.createTicket('Task Ticket', 'A task', 'task', 'low');
        await ticketsPage.expectTicketExists('Task Ticket');

        // Verify count
        const count = await ticketsPage.getTicketCount();
        expect(count).toBe(3);

        // Cleanup
        await ticketsPage.deleteTicket('Bug Ticket');
        await ticketsPage.deleteTicket('Feature Ticket');
        await ticketsPage.deleteTicket('Task Ticket');
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
        await page.waitForURL('/login', { timeout: 15000 });
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
        const rowCount = await page.locator('tr.data-row').count();
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

        // Fill form
        await page.locator('input[formcontrolname="email"]').fill(newUserEmail);
        await page.locator('input[formcontrolname="name"]').fill('New');
        await page.locator('input[formcontrolname="surname"]').fill('User');
        await page.locator('input[formcontrolname="password"]').fill('NewPassword123!');

        // Select role
        await page.locator('mat-select[formcontrolname="role"]').click();
        await page.locator('mat-option:has-text("User")').click();

        // In create mode button says "Create"
        await page.locator('mat-dialog-container button:has-text("Create")').click();

        // Wait for dialog to close
        await expect(dialog).toBeHidden({ timeout: 10000 });

        // Wait for table to reload - dialog closes only on success
        await page.waitForTimeout(2000);
    });
});

// ============================================
// NAVIGATION & AUTH GUARDS
// ============================================
test.describe('Navigation and Security', () => {
    test('Unauthenticated user redirected to login', async ({ page }) => {
        // Try to access protected routes without login
        await page.goto('/projects');
        await expect(page).toHaveURL('/login');

        await page.goto('/users');
        await expect(page).toHaveURL('/login');

        await page.goto('/tickets');
        await expect(page).toHaveURL('/login');
    });

    test('Navigate between pages', async ({ page }) => {
        const testEmail = `e2e-nav-${Date.now()}@example.com`;

        // Register
        const registerPage = new RegisterPage(page);
        await registerPage.goto();
        await registerPage.register(testEmail, 'Nav', 'User', 'Password123!');
        await expect(page).toHaveURL('/login', { timeout: 15000 });

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
        await expect(page).toHaveURL('/login');
    });

    test('Session persistence after page reload', async ({ page }) => {
        const testEmail = `e2e-session-${Date.now()}@example.com`;

        // Register
        const registerPage = new RegisterPage(page);
        await registerPage.goto();
        await registerPage.register(testEmail, 'Session', 'User', 'Password123!');
        await expect(page).toHaveURL('/login', { timeout: 15000 });

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