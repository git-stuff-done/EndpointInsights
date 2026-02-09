import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import { Observable} from "rxjs";
import {Job} from "../models/job.model";
import {TestItem} from "../models/test.model";
import {environment} from "../../environment";


@Injectable({ providedIn: 'root' })
export class JobsApi {
    constructor(private http: HttpClient) {}
    private baseUrl = '/api/jobs';

    getAllJobs(): Observable<Job[]>{
        return this.http.get<Job[]>(`${environment.apiUrl}/${this.baseUrl}`);
    }

    createJob(test:TestItem): Observable<TestItem>{
        return this.http.post<TestItem>(`${environment.apiUrl}/${this.baseUrl}`, test);
    }

    updateJob(id: string, test: TestItem): Observable<TestItem> {
        return this.http.put<TestItem>(`${environment.apiUrl}/${this.baseUrl}/${id}`, test);
    }

}