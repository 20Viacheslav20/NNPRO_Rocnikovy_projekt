export type TicketType = 'BUG' | 'FEATURE' | 'TASK';
export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH';
export type TicketState = 'OPEN' | 'IN_PROGRESS' | 'DONE';

export interface Ticket {
  id: string;               // UUID
  name: string;             // was: title
  description?: string | null;
  type: TicketType;
  priority: TicketPriority;
  state: TicketState;
  createdAt: string;
  // There is no project backend in the response: we take the projectId from the URL (route param)

}


export interface TicketCreateRequest {
  name: string;
  type: TicketType;
  priority: TicketPriority;
  description?: string | null;
}

export interface TicketUpdateRequest {
  name: string;
  type: TicketType;
  priority: TicketPriority;
  state: TicketState;
  description?: string | null;
}
