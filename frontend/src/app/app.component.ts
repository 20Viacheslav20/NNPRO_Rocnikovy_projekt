import { Component, OnInit } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MaterialModules } from './material.module';
import { AuthService, CurrentUser } from './core/services/auth.service'
import { Router } from '@angular/router';


@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, ...MaterialModules, RouterLink],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  user: CurrentUser | null = null;
  userInitials = '';

  constructor(private authService: AuthService, private router: Router) {
  }

  ngOnInit() {
    this.authService.currentUserObservable().subscribe(user => {
      this.user = user;
      this.userInitials = this.calcInitials(user);
    });
  }

  private calcInitials(user: CurrentUser | null): string {
    if (!user) return '';
    const n = user.name?.charAt(0) ?? '';
    const s = user.surname?.charAt(0) ?? '';
    return (s + n).toUpperCase();
  }

  isLoggedIn(): boolean {
    return !!this.authService.getToken();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  canShowAdminButton(): boolean {
    return this.authService.getUserRole() !== 'USER';
  }

  /** переход на нужную страницу */
  goToAdminPage(): void {
    this.router.navigate(['/users']);
  }
}