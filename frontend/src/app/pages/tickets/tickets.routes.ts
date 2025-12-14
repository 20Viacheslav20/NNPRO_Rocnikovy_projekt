import { Routes } from '@angular/router';
import { TicketsPageComponent } from './tickets-page/tickets-page.component';
import { TicketDetailComponent } from './ticket-detail/ticket-detail.component';
import { AuthGuard } from '../../core/auth-guard';

export const TICKETS_ROUTES: Routes = [
    {
        path: 'tickets',
        component: TicketsPageComponent,
        canActivate: [AuthGuard]
    },
    {
        path: 'projects/:projectId',
        canActivateChild: [AuthGuard],
        children: [
            { path: 'tickets', component: TicketsPageComponent },
            { path: 'tickets/:ticketId', component: TicketDetailComponent },
        ]
    }
];