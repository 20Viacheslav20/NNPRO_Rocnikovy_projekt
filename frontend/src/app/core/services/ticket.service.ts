import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Ticket, TicketRequest, TicketComment } from '../../pages/tickets/models/ticket.models';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TicketService {
    private readonly base = environment.apiUrl;

    constructor(private http: HttpClient) { }

    // /projects/{projectId}/tickets
    list(projectId: number): Observable<Ticket[]> {
        return this.http.get<Ticket[]>(`${this.base}/projects/${projectId}/tickets`);
    }

    get(projectId: number, ticketId: number): Observable<Ticket> {
        return this.http.get<Ticket>(`${this.base}/projects/${projectId}/tickets/${ticketId}`);
    }

    create(projectId: number, body: TicketRequest): Observable<Ticket> {
        return this.http.post<Ticket>(`${this.base}/projects/${projectId}/tickets`, body);
    }

    update(projectId: number, ticketId: number, body: TicketRequest): Observable<Ticket> {
        return this.http.put<Ticket>(`${this.base}/projects/${projectId}/tickets/${ticketId}`, body);
    }

    delete(projectId: number, ticketId: number): Observable<void> {
        return this.http.delete<void>(`${this.base}/projects/${projectId}/tickets/${ticketId}`);
    }

    exportCsv(projectId: number): Observable<Blob> {
        return this.http.get(`${this.base}/projects/${projectId}/tickets/export`, {
            responseType: 'blob'
        });
    }

    generate(projectId: number, count: number): Observable<{ generated: number }> {
        return this.http.post<{ generated: number }>(
            `${this.base}/projects/${projectId}/tickets/generate?count=${count}&projectId=${projectId}`,
            {}
        );
    }

    listComments(projectId: number, ticketId: number): Observable<TicketComment[]> {
        return this.http.get<TicketComment[]>(
            `${this.base}/projects/${projectId}/tickets/${ticketId}/comments`
        );
    }

    addComment(projectId: number, ticketId: number, message: string): Observable<TicketComment> {
        return this.http.post<TicketComment>(
            `${this.base}/projects/${projectId}/tickets/${ticketId}/comments`,
            { message }
        );
    }

    editComment(projectId: number, ticketId: number, commentId: number, message: string) {
        return this.http.put<TicketComment>(
            `${this.base}/projects/${projectId}/tickets/${ticketId}/comments/${commentId}`,
            { message }
        );
    }

    deleteComment(projectId: number, ticketId: number, commentId: number) {
        return this.http.delete<void>(
            `${this.base}/projects/${projectId}/tickets/${ticketId}/comments/${commentId}`
        );
    }
}
