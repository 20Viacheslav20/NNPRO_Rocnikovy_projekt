import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { DatePipe } from '@angular/common';
import { TicketService } from '../../../core/services/ticket.service';
import { TicketHistory } from '../ticket-history.model';

@Component({
    selector: 'app-ticket-history',
    standalone: true,
    imports: [CommonModule, MatCardModule, MatListModule, DatePipe],
    templateUrl: './ticket-history.component.html',
    styleUrls: ['./ticket-history.component.scss']
})
export class TicketHistoryComponent implements OnInit {
    @Input() projectId!: string;
    @Input() ticketId!: string;

    history: TicketHistory[] = [];
    loading = false;

    constructor(private service: TicketService) { }

    ngOnInit(): void {
        if (this.projectId && this.ticketId) {
            this.loadHistory();
        }
    }

    loadHistory(): void {
        this.loading = true;
        this.service.getHistory(this.projectId, this.ticketId)
            .subscribe({
                next: (data) => {
                    this.history = data;
                    this.loading = false;
                },
                error: () => {
                    this.history = [];
                    this.loading = false;
                }
            });
    }
}
