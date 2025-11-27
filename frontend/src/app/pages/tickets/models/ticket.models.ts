export type TicketType = 'bug' | 'feature' | 'task';
export type TicketPriority = 'low' | 'med' | 'high';
export type TicketState = 'open' | 'in_progress' | 'done';

export interface Ticket {
    id: string;
    name: string;
    type: TicketType;
    priority: TicketPriority;
    state: TicketState;
    createdAt: string;
    project: { id: string } | string;
    assigneeId?: number | null;
}

export interface TicketRequest {
    name: string;
    type: TicketType;
    priority: TicketPriority;
    state?: TicketState;
    assigneeId?: number | null;
}