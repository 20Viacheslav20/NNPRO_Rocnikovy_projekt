import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, of, tap } from 'rxjs';
import { environment } from '../../../environments/environment';


@Injectable({ providedIn: 'root' })
export class AuthService {

  private baseURL: string = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) {}


  login(email: string, password: string) {
    return this.http.post<string>(
      `${this.baseURL}/login`,
      { email, password },
      { responseType: 'text' as 'json' }
    ).pipe(
      tap(token => {
        localStorage.setItem('token', token);
      })
    );
  }

  register(email: string, password: string) {
    return this.http.post<string>(
      `${this.baseURL}/register`,
      {
        username: email,
        email,
        password
      },
      { responseType: 'text' as 'json' }
    );
  }

  logout() {
    localStorage.removeItem('token');
  }
}
