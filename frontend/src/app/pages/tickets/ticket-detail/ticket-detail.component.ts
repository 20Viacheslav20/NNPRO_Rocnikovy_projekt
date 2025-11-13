import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, ParamMap, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { Ticket, TicketRequest, TicketComment } from '../models/ticket.models';
import { TicketService } from '../../../core/services/ticket.service';
import { DateTimePipe } from '../../../shared/pipes/date-time.pipe';

@Component({
    selector: 'app-ticket-detail',
    standalone: true,
    imports: [
        CommonModule, RouterLink, FormsModule,
        MatCardModule, MatButtonModule, MatIconModule, DateTimePipe
    ],
    templateUrl: './ticket-detail.component.html',
    styleUrls: ['./ticket-detail.component.scss']
})
export class TicketDetailComponent {
    projectId!: number;
    ticketId!: number;
    ticket?: Ticket;

    comments: TicketComment[] = [];
    newComment = '';
    saving = false;

    constructor(
        private route: ActivatedRoute,
        private service: TicketService
    ) {
        this.route.paramMap.subscribe((p: ParamMap) => {
            this.projectId = Number(p.get('projectId'));
            this.ticketId = Number(p.get('ticketId'));
            this.loadTicket();
            this.loadComments();
        });
    }

    loadTicket(): void {
        this.service.get(this.projectId, this.ticketId).subscribe(t => this.ticket = t);
    }

    loadComments(): void {
        this.service.listComments(this.projectId, this.ticketId).subscribe(c => this.comments = c);
    }

    addComment(): void {
        const msg = this.newComment?.trim();
        if (!msg) return;
        this.saving = true;
        this.service.addComment(this.projectId, this.ticketId, msg).subscribe({
            next: () => {
                this.saving = false;
                this.newComment = '';
                this.loadComments();
            },
            error: () => this.saving = false
        });
    }

    getProjectId(): number {
        const p = this.ticket?.project;
        if (!p) return 0;

        if (typeof p === 'object' && 'id' in p) {
            return p.id;
        }

        return p as number;
    }

}
