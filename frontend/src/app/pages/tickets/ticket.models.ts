export type TicketType = 'bug' | 'feature' | 'task';
export type TicketPriority = 'low' | 'med' | 'high';
export type TicketState = 'open' | 'in_progress' | 'done';

export interface Ticket {
    id: string;
    name: string;
    description: string;
    type: TicketType;
    priority: TicketPriority;
    state: TicketState;
    createdAt: string;
    projectId?: string;
    assigneeId?: number | null;

    assignee?: UserShort | null;
    owner?: UserShort;
}

export interface TicketRequest {
    name: string;
    description: string;
    type: TicketType;
    priority: TicketPriority;
    state?: TicketState;
    assigneeId?: number | null;
}

export interface UserShort {
    id: string;
    username: string;
    name: string;
    surname: string;
}