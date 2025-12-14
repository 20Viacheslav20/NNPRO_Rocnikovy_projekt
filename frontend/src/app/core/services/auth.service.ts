import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { jwtDecode } from 'jwt-decode';
import { BehaviorSubject } from 'rxjs';

export interface CurrentUser {
  userId?: string;
  name: string;
  surname: string;
  email: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

  private baseURL: string = `${environment.apiUrl}/auth`;
  private user$ = new BehaviorSubject<CurrentUser | null>(null);

  constructor(private http: HttpClient) {
    this.restoreUser();
  }

  getUser() {
    return this.currentUser();
  }

  restoreUser() {
    const token = this.getToken();
    if (!token) return null;

    const user = this.decodeUserFromToken(token);

    this.user$.next(user);
    return user;
  }

  login(login: string, password: string) {
    this.logout();
    return this.http.post(
      `${this.baseURL}/login`,
      { login, password },
      { responseType: 'text' }
    ).pipe(
      tap(response => {
        const obj = JSON.parse(response);
        const token = obj.token;
        localStorage.setItem('token', token);
        const user = this.restoreUser();
        this.user$.next(user);
      })
    );
  }

  register(data: any) {
    this.logout();
    return this.http.post(`${this.baseURL}/register`, data).pipe(
      map(() => true)
    );
  }


  logout(): void {
    localStorage.removeItem('token');
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  currentUserObservable() {
    return this.user$.asObservable();
  }

  currentUser() {
    return this.user$.value;
  }

  isAuthenticated(): boolean {
    return !!localStorage.getItem('token');
  }

  getDefaultRouteByRole(role: string): string {
    if (role === 'USER') {
      return '/tickets';
    }
    return '/projects';
  }

  getUserRole(): string {
    return this.currentUser()?.role ?? '';
  }

  private decodeUserFromToken(token: string): CurrentUser {
    const decoded: any = jwtDecode(token);

    return {
      userId: decoded.userId,
      name: decoded.name,
      surname: decoded.surname,
      email: decoded.sub,
      role: decoded.role
    };
  }
}
