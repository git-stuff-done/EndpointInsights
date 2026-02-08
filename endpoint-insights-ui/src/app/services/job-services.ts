import {inject, Injectable} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {TestItem} from "../pages/test-overview/test-overview";
import {JobsApi} from "../jobsApi/jobsApi";
import {Job} from "../models/job.model";


@Injectable({
    providedIn: 'root'
})
export class JobService {
    private jobApi = inject(JobsApi);
    constructor() {}

    getAllJobs():Observable<Job[]>{
        return this.jobApi.getAllJobs();
    }

    createJob(test:TestItem): Observable<TestItem>{
        return this.jobApi.createJob(test);
    }

    updateJob(id: string, test: TestItem): Observable<TestItem> {
        return this.jobApi.updateJob(id, test);
    }
}