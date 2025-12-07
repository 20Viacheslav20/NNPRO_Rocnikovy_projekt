import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { User } from '../../shared/models/user.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UserService {

    private readonly baseURL = `${environment.apiUrl}/users`;

    constructor(private http: HttpClient) { }

    getAll(): Observable<User[]> {
        return this.http.get<User[]>(this.baseURL);
    }
}