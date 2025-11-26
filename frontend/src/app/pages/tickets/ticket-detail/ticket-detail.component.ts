import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { TicketService, Ticket, TicketUpdateRequest, TicketState } from '../../../core/services/ticket.service';

import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatOptionModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-ticket-detail',
  standalone: true,
  imports: [
    CommonModule, FormsModule, DatePipe,
    MatCardModule, MatProgressSpinnerModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatOptionModule, MatButtonModule, MatIconModule
  ],
  templateUrl: './ticket-detail.component.html',
  styleUrls: ['./ticket-detail.component.scss'],
})
export class TicketDetailComponent implements OnInit, OnDestroy {
  projectId!: string;
  ticketId!: string;

  ticket: Ticket | null = null;
  loading = false;
  error = '';

  readonly states: TicketState[] = ['OPEN', 'IN_PROGRESS', 'DONE'];

  private readonly destroy$ = new Subject<void>();

  constructor(private readonly route: ActivatedRoute, private readonly svc: TicketService) {}

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntil(this.destroy$)).subscribe((p: ParamMap) => {
      this.projectId = p.get('projectId')!;
      this.ticketId = p.get('ticketId')!;
      this.load();
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.svc.get(this.projectId, this.ticketId).pipe(takeUntil(this.destroy$)).subscribe({
      next: (t) => {
        this.ticket = t;
        this.loading = false;
      },
      error: (e) => {
        this.loading = false;
        this.error = e?.error?.message || 'Не удалось загрузить тикет.';
      },
    });
  }

  save(): void {
    if (!this.ticket) return;
    const body: TicketUpdateRequest = {
      name: (this.ticket.name ?? '').trim(),
      type: this.ticket.type,
      priority: this.ticket.priority,
      state: this.ticket.state,
      description: this.ticket.description ?? null,  // ← гарантированно string|null
    };
    this.loading = true;
    this.svc.update(this.projectId, this.ticket.id, body).subscribe({
      next: () => this.load(),
      error: (e) => {
        this.loading = false;
        this.error = e?.error?.message || 'Не удалось сохранить изменения.';
      },
    });
  }
}
