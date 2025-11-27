import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, ParamMap, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { Ticket } from '../models/ticket.models';
import { TicketService } from '../../../core/services/ticket.service';
import { DateTimePipe } from '../../../shared/pipes/date-time.pipe';
import { TicketTypePipe } from '../../../shared/pipes/ticketType.pipe';
import { TicketStatePipe } from '../../../shared/pipes/ticketState.pipe';
import { TicketPriorityPipe } from '../../../shared/pipes/ticketPriority.pipe';

@Component({
    selector: 'app-ticket-detail',
    standalone: true,
    imports: [
        CommonModule, RouterLink, FormsModule,
        MatCardModule, MatButtonModule, MatIconModule, DateTimePipe, TicketTypePipe,
        TicketStatePipe, TicketPriorityPipe
    ],
    templateUrl: './ticket-detail.component.html',
    styleUrls: ['./ticket-detail.component.scss']
})
export class TicketDetailComponent {
    projectId!: string;
    ticketId!: string;
    ticket?: Ticket;

    saving = false;

    constructor(
        private route: ActivatedRoute,
        private service: TicketService
    ) {
        this.route.paramMap.subscribe((p: ParamMap) => {
            this.projectId = p.get('projectId')!;
            this.ticketId = p.get('ticketId')!;
            this.loadTicket();
        });
    }

    loadTicket(): void {
        this.service.get(this.projectId, this.ticketId).subscribe(t => this.ticket = t);
    }

    getProjectId(): string {
        const p = this.ticket?.project;
        if (!p) return "";

        if (typeof p === 'object' && 'id' in p) {
            return p.id;
        }

        return p as string;
    }

}
