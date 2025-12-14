import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';
import { Ticket, TicketRequest } from '../ticket.models';
import { TicketService } from '../../../core/services/ticket.service';
import { DateTimePipe } from '../../../shared/pipes/date-time.pipe';
import { TicketTypePipe } from '../../../shared/pipes/ticketType.pipe';
import { TicketPriorityPipe } from '../../../shared/pipes/ticketPriority.pipe';
import { TicketStatePipe } from '../../../shared/pipes/ticketState.pipe';
import { TicketDialogComponent } from '../ticket-dialog/ticket-dialog.component';
import { MaterialModules } from '../../../material.module'
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-tickets-page',
    standalone: true,
    imports: [
        CommonModule, FormsModule,
        MaterialModules,
        DateTimePipe, TicketTypePipe, TicketPriorityPipe, TicketStatePipe
    ],
    templateUrl: './tickets-page.component.html',
    styleUrls: ['./tickets-page.component.scss']
})
export class TicketsPageComponent {
    projectId!: string;

    displayedColumns = ['name', 'type', 'priority', 'state', 'createdAt', 'owner', 'assignee', 'actions'];
    data: Ticket[] = [];

    search = '';
    typeFilter = '';
    stateFilter = '';
    priorityFilter = '';

    saving = false;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private service: TicketService,
        private dialog: MatDialog,
        private authService: AuthService
    ) {
        this.route.paramMap.subscribe((p: ParamMap) => {
            this.projectId = p.get('projectId')!;
            debugger
            if (this.projectId) {
                this.load();
            } else {
                this.loadMyTickets();
            }
        });
    }

    private loadMyTickets(): void {
        const userId = this.authService.currentUser()?.userId;

        if (!userId) {
            return;
        }

        this.service.getByAssignee(userId)
            .subscribe({
                next: (items) => { this.data = items; }
            });
    }

    private load(): void {
        this.service.list(this.projectId).subscribe({
            next: (items) => { this.data = items; }
        });
    }

    filtered(): Ticket[] {
        return this.data.filter(t => {
            const matchesSearch = this.search
                ? (t.name?.toLowerCase().includes(this.search.toLowerCase()))
                : true;
            const matchesType = this.typeFilter ? t.type === this.typeFilter : true;
            const matchesState = this.stateFilter ? t.state === this.stateFilter : true;
            const matchesPriority = this.priorityFilter ? t.priority === this.priorityFilter : true;
            return matchesSearch && matchesType && matchesState && matchesPriority;
        });
    }

    create(): void {
        const ref = this.dialog.open(TicketDialogComponent, {
            width: '60vw',
            maxWidth: 'none',
            data: { mode: 'create' as const }
        });

        ref.afterClosed().subscribe((result?: TicketRequest) => {
            if (!result) return;
            this.saving = true;
            this.service.create(this.projectId, result).subscribe({
                next: () => { this.saving = false; this.load(); },
                error: () => { this.saving = false; }
            });
        });
    }

    edit(row: Ticket): void {
        const ref = this.dialog.open(TicketDialogComponent, {
            width: '60vw',
            maxWidth: 'none',
            data: {
                mode: 'edit' as const,
                ticket: row
            }
        });

        ref.afterClosed().subscribe((result?: TicketRequest) => {
            if (!result) return;
            this.saving = true;
            this.service.update(this.projectId, row.id, result).subscribe({
                next: () => { this.saving = false; this.load(); },
                error: () => { this.saving = false; }
            });
        });
    }


    view(row: Ticket): void {
        if (this.projectId) {
            this.router.navigate(['/projects', this.projectId, 'tickets', row.id]);
        } else {
            this.router.navigate(['/projects', row.projectId, 'tickets', row.id]);
        }

    }

    remove(row: Ticket): void {
        if (!confirm(`Delete ticket #${row.id}?`)) return;
        this.saving = true;
        this.service.delete(this.projectId, row.id).subscribe({
            next: () => { this.saving = false; this.load(); },
            error: () => { this.saving = false; }
        });
    }

    typeClass(type: string): string {
        switch (type) {
            case 'bug': return 'chip-bug';
            case 'feature': return 'chip-feature';
            case 'task': return 'chip-task';
            default: return '';
        }
    }

    priorityClass(priority: string): string {
        switch (priority) {
            case 'low': return 'chip-low';
            case 'med': return 'chip-medium';
            case 'high': return 'chip-high';
            default: return '';
        }
    }

    stateClass(state: string): string {
        switch (state) {
            case 'open': return 'chip-open';
            case 'in_progress': return 'chip-progress';
            case 'done': return 'chip-done';
            default: return '';
        }
    }


}
