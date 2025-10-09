import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, of, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

interface LoginResponse {
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

  private baseURL: string = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) {}


  login(email: string, password: string) {
    return this.http.post<LoginResponse>(`${this.baseURL}/login`, { email, password }).pipe(
      tap(res => {
        localStorage.setItem('token', res.token);
      }),
      map(() => true),
      catchError(() => of(false))
    );
  }

  register(email: string, password: string) {
    return this.http.post(`${this.baseURL}/register`, { email, password }).pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }

  logout() {
    localStorage.removeItem('token');
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('token');
    return new HttpHeaders({
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    });
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }
}
