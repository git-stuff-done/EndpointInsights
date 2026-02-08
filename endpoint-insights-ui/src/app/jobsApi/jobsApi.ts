import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {ToastService} from "../services/toast.service";
import { Observable} from "rxjs";
import {Job} from "../models/job.model";
import {TestItem} from "../models/test.model";


@Injectable({ providedIn: 'root' })
export class JobsApi {
    constructor(private http: HttpClient) {}
    private baseUrl = 'http://localhost:8080/api/jobs';
    private toast = inject(ToastService);

    getAllJobs(): Observable<Job[]>{
        return this.http.get<Job[]>(this.baseUrl);
    }

    createJob(test:TestItem): Observable<TestItem>{
        return this.http.post<TestItem>(this.baseUrl, test);
    }

    updateJob(id: string, test: TestItem): Observable<TestItem> {
        return this.http.put<TestItem>(`${this.baseUrl}/${id}`, test);
    }

}