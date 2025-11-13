import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { Project } from '../../pages/projects/models/project.model';
import { ProjectRequest } from '../../pages/projects/models/project-request.model';

@Injectable({ providedIn: 'root' })
export class ProjectService {

    private http = inject(HttpClient);
    private baseURL = `${environment.apiUrl}/projects`;

    getAll(): Observable<Project[]> {
        return this.http.get<Project[]>(this.baseURL);
    }

    getById(id: number): Observable<Project> {
        return this.http.get<Project>(`${this.baseURL}/${id}`);
    }

    create(body: ProjectRequest): Observable<Project> {
        return this.http.post<Project>(this.baseURL, body);
    }

    update(id: number, body: ProjectRequest): Observable<Project> {
        return this.http.put<Project>(`${this.baseURL}/${id}`, body);
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.baseURL}/${id}`);
    }
}
