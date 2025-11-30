import { Pipe, PipeTransform } from '@angular/core';
import { TicketPriority } from '../../pages/tickets/ticket.models';

@Pipe({ name: 'ticketPriority', standalone: true })
export class TicketPriorityPipe implements PipeTransform {

    transform(value: TicketPriority): string {
        const map: Record<TicketPriority, string> = {
            low: 'Low',
            med: 'Medium',
            high: 'High'
        };

        return map[value] ?? value;
    }
}
