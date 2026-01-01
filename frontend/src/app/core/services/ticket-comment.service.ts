import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TicketComment, TicketCommentRequest } from '../../pages/tickets/ticket-comment.model';
import { environment } from '../../../environments/environment';


@Injectable({ providedIn: 'root' })
export class TicketCommentService {


    private readonly baseUrl = environment.apiUrl;

    constructor(private http: HttpClient) { }

    list(ticketId: string, projectId: string): Observable<TicketComment[]> {
        return this.http.get<TicketComment[]>(`${this.baseUrl}/projects/${projectId}/tickets/${ticketId}/comments`);
    }

    create(ticketId: string, req: TicketCommentRequest, projectId: string): Observable<TicketComment> {
        return this.http.post<TicketComment>(`${this.baseUrl}/projects/${projectId}/tickets/${ticketId}/comments`, req);
    }

    update(ticketId: string, commentId: string, req: TicketCommentRequest, projectId: string): Observable<TicketComment> {
        return this.http.put<TicketComment>(
            `${this.baseUrl}/projects/${projectId}/tickets/${ticketId}/comments/${commentId}`,
            req
        );
    }

    delete(ticketId: string, commentId: string, projectId: string): Observable<void> {
        return this.http.delete<void>(
            `${this.baseUrl}/projects/${projectId}/tickets/${ticketId}/comments/${commentId}`
        );
    }
}
