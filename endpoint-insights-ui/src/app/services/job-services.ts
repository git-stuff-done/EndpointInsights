import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {TestItem} from "../pages/test-overview/test-overview";


@Injectable({
    providedIn: 'root'
})
export class JobService {
    private apiUrl = 'http://localhost:8080/api/jobs';

    constructor(private http: HttpClient) {}

    createJob(test:TestItem): Observable<TestItem>{
        return this.http.post<TestItem>(this.apiUrl, test);
    }

    updateJob(id: number, test: TestItem): Observable<TestItem> {
        return this.http.put<TestItem>(`${this.apiUrl}/${id}`, test);
    }
}