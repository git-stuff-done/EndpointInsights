import {inject, Injectable} from "@angular/core";
import {HttpClient, HttpResponse} from "@angular/common/http";
import { Observable} from "rxjs";
import {Job} from "../models/job.model";
import {TestItem} from "../models/test.model";
import {environment} from "../../environment";
import {HttpInterceptorService} from "../services/http-interceptor.service";
import {ApiTest} from "../batch-component/components/batch-config-dialog/batch-config-dialog.component";


@Injectable({ providedIn: 'root' })
export class JobsApi {
    constructor(private http: HttpClient) {}
    private baseUrl = '/jobs';
    private httpInterceptService = inject(HttpInterceptorService);

    getAllJobs(): Observable<HttpResponse<ApiTest[]>>{
        return this.httpInterceptService.get<ApiTest[]>(`${environment.apiUrl}${this.baseUrl}`);
    }

    createJob(test:TestItem): Observable<HttpResponse<TestItem>>{
        return this.httpInterceptService.post<TestItem>(`${environment.apiUrl}${this.baseUrl}`, test);
    }

    updateJob(id: string, test: TestItem): Observable<HttpResponse<TestItem>> {
        return this.httpInterceptService.put<TestItem>(`${environment.apiUrl}${this.baseUrl}/${id}`, test);
    }

}