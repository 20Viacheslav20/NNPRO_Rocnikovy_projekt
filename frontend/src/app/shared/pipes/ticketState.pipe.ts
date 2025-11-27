import { Pipe, PipeTransform } from '@angular/core';
import { TicketState } from '../../../app/pages/tickets/models/ticket.models';

@Pipe({ name: 'ticketState', standalone: true })
export class TicketStatePipe implements PipeTransform {

    transform(value: TicketState): string {
        const map: Record<TicketState, string> = {
            open: 'Open',
            in_progress: 'In Progress',
            done: 'Done'
        };

        return map[value] ?? value;
    }
}
