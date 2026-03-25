import {inject, Injectable} from "@angular/core";
import {HttpResponse} from "@angular/common/http";
import {Observable} from "rxjs";
import {TestItem} from "../models/test.model";
import {environment} from "../../environment";
import {HttpInterceptorService} from "../services/http-interceptor.service";


@Injectable({ providedIn: 'root' })
export class JobsApi {
    constructor() {}
    private baseUrl = '/jobs';
    private httpInterceptService = inject(HttpInterceptorService);

    getAllJobs(): Observable<HttpResponse<TestItem[]>>{
        return this.httpInterceptService.get<TestItem[]>(`${environment.apiUrl}${this.baseUrl}`)
    }

    createJob(test:TestItem): Observable<HttpResponse<TestItem>>{
        return this.httpInterceptService.post<TestItem>(`${environment.apiUrl}${this.baseUrl}`, test);
    }

    updateJob(id: string, test: TestItem): Observable<HttpResponse<TestItem>> {
        return this.httpInterceptService.put<TestItem>(`${environment.apiUrl}${this.baseUrl}/${id}`, test);
    }

}