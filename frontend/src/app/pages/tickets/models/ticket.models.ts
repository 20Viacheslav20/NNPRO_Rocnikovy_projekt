export type TicketType = 'BUG' | 'FEATURE' | 'TASK';
export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH';
export type TicketState = 'OPEN' | 'IN_PROGRESS' | 'DONE';

export interface Ticket {
    id: number;
    title: string;
    type: TicketType;
    priority: TicketPriority;
    state: TicketState;
    updatedAt: string;
    project: { id: number } | number;
    assigneeId?: number | null;
}

export interface TicketRequest {
    title: string;
    type: TicketType;
    priority: TicketPriority;
    state?: TicketState;
    assigneeId?: number | null;
}

export interface TicketComment {
    id: number;
    message: string;
    author?: { id: number; email: string } | null;
    createdAt?: string;
}
