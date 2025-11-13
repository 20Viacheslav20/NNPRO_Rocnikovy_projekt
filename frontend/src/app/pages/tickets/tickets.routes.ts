import { Routes } from '@angular/router';
import { TicketsPageComponent } from './tickets-page/tickets-page.component';
import { TicketDetailComponent } from './ticket-detail/ticket-detail.component';

export const TICKETS_ROUTES: Routes = [
    { path: 'projects/:projectId/tickets', component: TicketsPageComponent },
    { path: 'projects/:projectId/tickets/:ticketId', component: TicketDetailComponent },
];
