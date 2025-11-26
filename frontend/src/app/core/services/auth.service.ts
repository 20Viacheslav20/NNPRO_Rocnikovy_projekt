import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { map, switchMap } from 'rxjs/operators';
import { Observable, of } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private baseURL = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<void> {
    return this.http.post(
      `${this.baseURL}/login`,
      { login: email, password },
      { withCredentials: true, observe: 'response', responseType: 'text' }
    ).pipe(
      map((resp: HttpResponse<string>) => {
        const token = this.extractToken(resp);
        if (token) localStorage.setItem('token', token);
        return void 0;                       // ← возвращаем void
      }),
      // опционально «прогреть» cookie XSRF:
      switchMap(() => this.http.get(`${environment.apiUrl}/projects`, { withCredentials: true }).pipe(
        map(() => void 0)                    // ← снова приводим к void
      ))
    );
  }

  register(data: any): Observable<void> {
    return this.http.post(`${this.baseURL}/register`, data, { withCredentials: true })
      .pipe(map(() => void 0));
  }

  logout(): void {
    localStorage.removeItem('token');
  }

  // --- helpers ---
  private extractToken(resp: HttpResponse<string>): string | null {
    let raw = resp.body ?? '';
    if (raw.trim().startsWith('{')) {
      try { raw = JSON.parse(raw).token || ''; } catch {}
    }
    const hdr = resp.headers.get('Authorization') || '';
    const combined = (raw || hdr).trim().replace(/^"+|"+$/g, '').replace(/^Bearer\s+/i, '');
    const m = combined.match(/([A-Za-z0-9\-_]+\.[A-Za-z0-9\-_]+\.[A-Za-z0-9\-_]+)/);
    return m ? m[1] : null;
  }
}
