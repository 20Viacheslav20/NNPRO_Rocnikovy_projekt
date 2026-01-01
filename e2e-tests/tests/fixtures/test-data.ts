/**
 * Test data for E2E tests
 */

export const testUser = {
  email: `e2e-test-${Date.now()}@example.com`,
  name: 'E2E',
  surname: 'TestUser',
  password: 'Password123!'
};

export const testProject = {
  name: `E2E Test Project ${Date.now()}`,
  description: 'Project created during E2E testing'
};

export const testTicket = {
  name: 'E2E Test Bug',
  description: 'Bug found during E2E testing',
  type: 'bug' as const,
  priority: 'high' as const
};

export const updatedTicket = {
  name: 'E2E Test Bug - Updated',
  description: 'Bug in progress',
  type: 'bug' as const,
  priority: 'high' as const,
  state: 'in_progress' as const
};

// Types
export type TicketType = 'bug' | 'feature' | 'task';
export type TicketPriority = 'low' | 'med' | 'high';
export type TicketState = 'open' | 'in_progress' | 'done';

export interface TestUser {
  email: string;
  name: string;
  surname: string;
  password: string;
}

export interface TestProject {
  name: string;
  description: string;
}

export interface TestTicket {
  name: string;
  description: string;
  type: TicketType;
  priority: TicketPriority;
  state?: TicketState;
}
