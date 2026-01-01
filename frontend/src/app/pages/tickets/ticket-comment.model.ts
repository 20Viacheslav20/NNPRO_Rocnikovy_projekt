export interface TicketComment {
    id: string;
    ticketId: string;
    commentAuthorId: string;
    authorName?: string;
    text: string;
    createdAt: string;
}

export interface TicketCommentRequest {
    ticketId: string;
    text: string;
}
