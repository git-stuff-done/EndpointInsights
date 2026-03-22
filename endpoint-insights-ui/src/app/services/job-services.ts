import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { TestItem } from '../models/test.model';
import { environment } from '../../environment';
import { HttpInterceptorService } from './http-interceptor.service';


@Injectable({
    providedIn: 'root'
})
export class JobService {
    private apiUrl = `${environment.apiUrl}/jobs`;

    constructor(
        private http: HttpClient,
        private httpInterceptor: HttpInterceptorService,
    ) {}

    getAllJobs(): Observable<any[]> {
        return this.httpInterceptor.get<any[]>(this.apiUrl).pipe(map(r => r.body ?? []));
    }

    createJob(test: TestItem): Observable<TestItem> {
        return this.httpInterceptor.post<TestItem>(this.apiUrl, test).pipe(map(r => r.body!));
    }

    updateJob(id: string, test: TestItem): Observable<TestItem> {
        return this.httpInterceptor.put<TestItem>(`${this.apiUrl}/${id}`, test).pipe(map(r => r.body!));
    }

    deleteJob(id: string): Observable<any> {
        return this.httpInterceptor.delete<any>(`${this.apiUrl}/${id}`).pipe(map(r => r.body));
    }

    runJob(id: string): Observable<any> {
        return this.httpInterceptor.post<any>(`${this.apiUrl}/${id}/run`, {}).pipe(map(r => r.body));
    }
}