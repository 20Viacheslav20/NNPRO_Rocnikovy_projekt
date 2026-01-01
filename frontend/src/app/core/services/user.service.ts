import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { User } from '../../shared/models/user.model';
import { Observable } from 'rxjs';
import { UserRequest } from '../../pages/users/model/user-request.model'

@Injectable({ providedIn: 'root' })
export class UserService {

    private readonly baseURL = `${environment.apiUrl}/users`;

    constructor(private http: HttpClient) { }

    getAll(): Observable<User[]> {
        return this.http.get<User[]>(this.baseURL);
    }

    getById(id: string): Observable<User> {
        return this.http.get<User>(`${this.baseURL}/${id}`);
    }

    create(req: UserRequest): Observable<User> {
        return this.http.post<User>(this.baseURL, req);
    }

    update(id: string, req: UserRequest): Observable<User> {
        return this.http.put<User>(`${this.baseURL}/${id}`, req);
    }

    delete(id: string): Observable<void> {
        return this.http.delete<void>(`${this.baseURL}/${id}`);
    }

    blockUser(userId: string): Observable<HttpResponse<void>> {
        return this.http.post<void>(`${this.baseURL}/${userId}/block`, null, { observe: 'response' });
    }

    unblockUser(userId: string): Observable<HttpResponse<void>> {
        return this.http.post<void>(`${this.baseURL}/${userId}/unblock`, null, { observe: 'response' });
    }
}