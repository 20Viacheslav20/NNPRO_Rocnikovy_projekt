// src/app/pages/tickets/tickets-page/tickets-page.component.ts
import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import {
  TicketService,
  Ticket,
  TicketCreateRequest,
  TicketUpdateRequest,
  TicketPriority,
  TicketType,
  TicketState,
} from '../../../core/services/ticket.service';

// material
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-tickets-page',
  standalone: true,
  imports: [
    CommonModule, FormsModule, DatePipe,
    MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatOptionModule,
    MatIconModule, MatTableModule, MatButtonModule, MatTooltipModule, MatProgressSpinnerModule,
  ],
  templateUrl: './tickets-page.component.html',
  styleUrls: ['./tickets-page.component.scss'],
})
export class TicketsPageComponent implements OnInit, OnDestroy {
  projectId: string | null = null;

  data: Ticket[] = [];
  filtered: Ticket[] = [];
  displayedColumns: string[] = ['id', 'name', 'type', 'priority', 'state', 'createdAt', 'actions'];

  loading = false;
  info = '';
  error = '';

  search = '';

  formName = '';
  formType: TicketType = 'TASK';
  formPriority: TicketPriority = 'MEDIUM';
  formDescription: string | null = null;

  readonly types: TicketType[] = ['BUG', 'FEATURE', 'TASK'];
  readonly priorities: TicketPriority[] = ['LOW', 'MEDIUM', 'HIGH'];
  readonly states: TicketState[] = ['OPEN', 'IN_PROGRESS', 'DONE'];

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly ticketService: TicketService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.pipe(takeUntil(this.destroy$)).subscribe((p: ParamMap) => {
      this.projectId = p.get('projectId');
      this.load(this.projectId ?? undefined);
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private load(projectId?: string): void {
    this.error = '';
    this.info = '';
    this.loading = true;

    if (!projectId) {
      this.data = [];
      this.filtered = [];
      this.loading = false;
      this.info = 'Просмотр тикетов без выбора проекта недоступен: в API нет такого эндпоинта.';
      return;
    }

    this.ticketService.list(projectId).pipe(takeUntil(this.destroy$)).subscribe({
      next: (rows) => {
        this.data = rows ?? [];
        this.applySearch();
        this.loading = false;
      },
      error: (e) => {
        this.loading = false;
        this.error = e?.error?.message || 'Не удалось загрузить тикеты.';
        this.data = [];
        this.filtered = [];
      },
    });
  }

  // поиск
  onSearchChange(): void {
    this.applySearch();
  }

  private applySearch(): void {
    const q = this.search?.trim().toLowerCase();
    if (!q) {
      this.filtered = this.data.slice();
      return;
    }
    this.filtered = this.data.filter((row) => this.matchesSearch(row, q));
  }

  private matchesSearch(row: Ticket, q: string): boolean {
    return (
      row.id.toLowerCase().includes(q) ||
      (row.name ?? '').toLowerCase().includes(q) ||
      (row.description ?? '').toLowerCase().includes(q) ||
      row.type.toLowerCase().includes(q) ||
      row.priority.toLowerCase().includes(q) ||
      row.state.toLowerCase().includes(q)
    );
  }

  trackById(_: number, row: Ticket): string {
    return row.id;
  }

  // переход к детали
  open(row: Ticket): void {
    if (!this.projectId) return;
    this.router.navigate(['/projects', this.projectId, 'tickets', row.id]);
  }

  // CRUD
  create(): void {
    if (!this.projectId) { this.error = 'Сначала выберите проект.'; return; }
    const body: TicketCreateRequest = {
      name: this.formName?.trim(),
      type: this.formType,
      priority: this.formPriority,
      description: this.formDescription ?? null,   // ← не undefined
    };
    if (!body.name) { this.error = 'Название тикета не должно быть пустым.'; return; }

    this.loading = true;
    this.ticketService.create(this.projectId, body).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => { this.resetCreateForm(); this.load(this.projectId!); },
      error: (e) => { this.loading = false; this.error = e?.error?.message || 'Не удалось создать тикет.'; },
    });
  }

  saveEdit(row: Ticket): void {
    if (!this.projectId) { this.error = 'Сначала выберите проект.'; return; }
    const payload: TicketUpdateRequest = {
      name: (row.name ?? '').trim(),
      type: row.type,
      priority: row.priority,
      state: row.state,
      description: row.description ?? null,        // ← не undefined
    };
    if (!payload.name) { this.error = 'Название тикета не должно быть пустым.'; return; }

    this.loading = true;
    this.ticketService.update(this.projectId, row.id, payload).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => this.load(this.projectId!),
      error: (e) => { this.loading = false; this.error = e?.error?.message || 'Не удалось сохранить изменения.'; },
    });
  }

  remove(row: Ticket): void {
    if (!this.projectId) {
      this.error = 'Сначала выберите проект.';
      return;
    }
    this.loading = true;
    this.ticketService.delete(this.projectId, row.id).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => this.load(this.projectId!),
      error: (e) => {
        this.loading = false;
        this.error = e?.error?.message || 'Не удалось удалить тикет.';
      },
    });
  }

  setState(row: Ticket, state: TicketState): void {
    row.state = state;
    this.saveEdit(row);
  }

  private resetCreateForm(): void {
    this.formName = '';
    this.formType = 'TASK';
    this.formPriority = 'MEDIUM';
    this.formDescription = null;
  }
}
