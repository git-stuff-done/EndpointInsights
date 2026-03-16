import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import { Observable } from 'rxjs';
import {TestItem} from "../models/test.model";
import {HttpInterceptorService} from "./http-interceptor.service";


@Injectable({
    providedIn: 'root'
})
export class JobService {
    private apiUrl = 'http://localhost:8080/api/jobs';

    constructor(private http: HttpClient) {}
    private httpInterceptService = inject(HttpInterceptorService);

    createJob(test:TestItem): Observable<HttpResponse<TestItem>>{
        return this.httpInterceptService.post<TestItem>(this.apiUrl, test);
    }

    updateJob(id: string, test: TestItem): Observable<HttpResponse<TestItem>> {
        return this.httpInterceptService.put<TestItem>(`${this.apiUrl}/${id}`, test);
    }
}