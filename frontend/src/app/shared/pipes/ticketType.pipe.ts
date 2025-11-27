import { Pipe, PipeTransform } from '@angular/core';
import { TicketType } from '../../../app/pages/tickets/models/ticket.models';

@Pipe({ name: 'ticketType', standalone: true })
export class TicketTypePipe implements PipeTransform {

    transform(value: TicketType): string {
        debugger
        const map: Record<TicketType, string> = {
            bug: 'Bug',
            feature: 'Feature',
            task: 'Task'
        };

        return map[value] ?? value;
    }
}
