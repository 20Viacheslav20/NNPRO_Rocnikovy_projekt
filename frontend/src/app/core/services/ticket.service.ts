// src/app/core/services/ticket.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

export type TicketPriority = 'LOW' | 'MEDIUM' | 'HIGH';
export type TicketType = 'BUG' | 'FEATURE' | 'TASK';
export type TicketState = 'OPEN' | 'IN_PROGRESS' | 'DONE';

export interface Ticket {
  id: string;
  name: string;
  type: TicketType;
  priority: TicketPriority;
  state: TicketState;
  description?: string | null;
  createdAt: string;
}

export interface TicketCreateRequest {
  name: string;
  type: TicketType;
  priority: TicketPriority;
  description: string | null;
}

export interface TicketUpdateRequest extends TicketCreateRequest {
  state: TicketState;
}

@Injectable({ providedIn: 'root' })
export class TicketService {
  private base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  list(projectId: string): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(
      `${this.base}/projects/${projectId}/tickets`,
      { withCredentials: true }
    );
  }

  get(projectId: string, ticketId: string): Observable<Ticket> {
    return this.http.get<Ticket>(
      `${this.base}/projects/${projectId}/tickets/${ticketId}`,
      { withCredentials: true }
    );
  }

  create(projectId: string, payload: TicketCreateRequest): Observable<Ticket> {
    return this.http.post<Ticket>(
      `${this.base}/projects/${projectId}/tickets`,
      payload,
      { withCredentials: true }
    );
  }

  update(projectId: string, ticketId: string, payload: TicketUpdateRequest): Observable<Ticket> {
    return this.http.put<Ticket>(
      `${this.base}/projects/${projectId}/tickets/${ticketId}`,
      payload,
      { withCredentials: true }
    );
  }

  delete(projectId: string, ticketId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.base}/projects/${projectId}/tickets/${ticketId}`,
      { withCredentials: true }
    );
  }
}
