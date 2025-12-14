import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Ticket, TicketRequest } from '../../pages/tickets/ticket.models';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TicketService {
    private readonly base = environment.apiUrl;

    constructor(private http: HttpClient) { }

    // /projects/{projectId}/tickets
    list(projectId: string): Observable<Ticket[]> {
        return this.http.get<Ticket[]>(`${this.base}/projects/${projectId}/tickets`);
    }

    get(projectId: string, ticketId: string): Observable<Ticket> {
        return this.http.get<Ticket>(`${this.base}/projects/${projectId}/tickets/${ticketId}`);
    }


    getByAssignee(userId: string) {
        return this.http.get<Ticket[]>(
            `${environment.apiUrl}/tickets/assignee/${userId}`
        );
    }

    create(projectId: string, body: TicketRequest): Observable<Ticket> {
        return this.http.post<Ticket>(`${this.base}/projects/${projectId}/tickets`, body);
    }

    update(projectId: string, ticketId: string, body: TicketRequest): Observable<Ticket> {
        return this.http.put<Ticket>(`${this.base}/projects/${projectId}/tickets/${ticketId}`, body);
    }

    delete(projectId: string, ticketId: string): Observable<void> {
        return this.http.delete<void>(`${this.base}/projects/${projectId}/tickets/${ticketId}`);
    }
}
