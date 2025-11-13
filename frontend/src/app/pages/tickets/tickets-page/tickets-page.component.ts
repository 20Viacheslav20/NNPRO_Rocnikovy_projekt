import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, ParamMap, Router, RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { Ticket, TicketRequest } from '../models/ticket.models';
import { TicketService } from '../../../core/services/ticket.service';
import { DateTimePipe } from '../../../shared/pipes/date-time.pipe';
import { TicketDialogComponent } from '../ticket-dialog/ticket-dialog.component';

@Component({
    selector: 'app-tickets-page',
    standalone: true,
    imports: [
        CommonModule, RouterLink, FormsModule,
        MatTableModule, MatButtonModule, MatIconModule, MatDialogModule,
        MatFormFieldModule, MatSelectModule, MatInputModule,
        DateTimePipe
    ],
    templateUrl: './tickets-page.component.html',
    styleUrls: ['./tickets-page.component.scss']
})
export class TicketsPageComponent {
    projectId!: number;

    displayedColumns = ['id', 'title', 'type', 'priority', 'state', 'updatedAt', 'assignee', 'actions'];
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
        private dialog: MatDialog
    ) {
        this.route.paramMap.subscribe((p: ParamMap) => {
            this.projectId = Number(p.get('projectId'));
            this.load();
        });
    }

    load(): void {
        this.service.list(this.projectId).subscribe({
            next: (items) => this.data = items,
            error: () => { }
        });
    }

    filtered(): Ticket[] {
        return this.data.filter(t => {
            const matchesSearch = this.search
                ? (t.title?.toLowerCase().includes(this.search.toLowerCase()) || String(t.id).includes(this.search))
                : true;
            const matchesType = this.typeFilter ? t.type === this.typeFilter : true;
            const matchesState = this.stateFilter ? t.state === this.stateFilter : true;
            const matchesPriority = this.priorityFilter ? t.priority === this.priorityFilter : true;
            return matchesSearch && matchesType && matchesState && matchesPriority;
        });
    }

    create(): void {
        const ref = this.dialog.open(TicketDialogComponent, {
            width: '520px',
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
            width: '520px',
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
        this.router.navigate(['/projects', this.projectId, 'tickets', row.id]);
    }

    remove(row: Ticket): void {
        if (!confirm(`Delete ticket #${row.id}?`)) return;
        this.saving = true;
        this.service.delete(this.projectId, row.id).subscribe({
            next: () => { this.saving = false; this.load(); },
            error: () => { this.saving = false; }
        });
    }

    exportCsv(): void {
        this.service.exportCsv(this.projectId).subscribe(blob => {
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'tickets.csv';
            a.click();
            URL.revokeObjectURL(url);
        });
    }

    generate(): void {
        this.saving = true;
        this.service.generate(this.projectId, 10).subscribe({
            next: () => { this.saving = false; this.load(); },
            error: () => { this.saving = false; }
        });
    }

    typeClass(type: string): string {
        switch (type) {
            case 'BUG': return 'chip--bug';
            case 'FEATURE': return 'chip--feature';
            case 'TASK': return 'chip--task';
            default: return '';
        }
    }

    priorityClass(priority: string): string {
        switch (priority) {
            case 'LOW': return 'chip--low';
            case 'MEDIUM': return 'chip--medium';
            case 'HIGH': return 'chip--high';
            default: return '';
        }
    }

    stateClass(state: string): string {
        switch (state) {
            case 'OPEN': return 'chip--open';
            case 'IN_PROGRESS': return 'chip--progress';
            case 'DONE': return 'chip--done';
            default: return '';
        }
    }
}
