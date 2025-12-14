import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { ProjectsPageComponent } from './pages/projects/projects-page/projects-page.component';
import { TICKETS_ROUTES } from './pages/tickets/tickets.routes';
import { UsersPageComponent } from './pages/users/users-page/users-page.component';
import { AuthGuard } from './core/auth-guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: 'projects',
    component: ProjectsPageComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'users',
    component: UsersPageComponent,
    canActivate: [AuthGuard]
  },
  ...TICKETS_ROUTES,
];
