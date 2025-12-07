import { Pipe, PipeTransform } from '@angular/core';
import { TicketType } from '../../pages/tickets/ticket.models';

@Pipe({ name: 'ticketType', standalone: true })
export class TicketTypePipe implements PipeTransform {

    transform(value: TicketType): string {
        const map: Record<TicketType, string> = {
            bug: 'Bug',
            feature: 'Feature',
            task: 'Task'
        };

        return map[value] ?? value;
    }
}
