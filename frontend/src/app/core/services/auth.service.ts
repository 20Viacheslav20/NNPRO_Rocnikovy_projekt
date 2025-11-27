import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, map, of, tap } from 'rxjs';
import { environment } from '../../../environments/environment';


@Injectable({ providedIn: 'root' })
export class AuthService {

  private baseURL: string = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient) { }

  login(login: string, password: string) {
    return this.http.post(
      `${this.baseURL}/login`,
      { login, password },
      { responseType: 'text' }
    ).pipe(
      tap(response => {
        const obj = JSON.parse(response);
        const token = obj.token;

        console.log(token);
        localStorage.setItem('token', token);
      })
    );
  }

  register(data: any) {
    return this.http.post(`${this.baseURL}/register`, data).pipe(
      map(() => true)
    );
  }


  logout() {
    localStorage.removeItem('token');
  }
}
