export interface TicketHistory {
    id: string;
    authorId: string;
    action: 'CREATED' | 'UPDATED' | 'DELETED';
    field?: string;
    oldValue?: string;
    newValue?: string;
    createdAt: string;
}
