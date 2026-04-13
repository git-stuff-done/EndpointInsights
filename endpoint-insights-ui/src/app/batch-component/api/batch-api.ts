import {inject, Injectable} from "@angular/core";
import {HttpClient, HttpResponse} from "@angular/common/http";
import {Batch} from "../../models/batch.model";
import {catchError, Observable, tap, throwError} from "rxjs";
import {ToastService} from "../../services/toast.service";
import {environment} from "../../../environment";
import {HttpInterceptorService} from "../../services/http-interceptor.service";
import {TestRun} from "../../models/test-run.model";


@Injectable({ providedIn: 'root' })
export class BatchApi {
    constructor(private http: HttpClient) {}
    private toast = inject(ToastService);
    private httpInterceptService = inject(HttpInterceptorService);

    getAllBatches(): Observable<HttpResponse<Batch[]>>{
        return this.httpInterceptService.get<Batch[]>(`${environment.apiUrl}/batches`)
    }

    getBatchById(id: string){
        return this.http.get<Batch>(`${environment.apiUrl}/batches/${id}`)
    }

    deleteBatch(id: string){
        return this.httpInterceptService.delete<Batch>(`${environment.apiUrl}/batches/${id}`)
    }

    runBatch(id: string) {
        return this.httpInterceptService.post<TestRun>(`${environment.apiUrl}/batches/${id}/run`, null);
    }

    saveBatch(batch: Batch):Observable<HttpResponse<Batch>>{
        if(batch.isNew){
            const createRequest = {
                batchName: batch.batchName,
                jobs: (batch.jobs ?? []).map((j: any) => j.jobId),
                emails: batch.emails ?? [],
                groupIds: batch.groupIds ?? [],
                active: batch.active,
            };
            return this.httpInterceptService.post<Batch>(`${environment.apiUrl}/batches`, createRequest)
                .pipe(
                    tap(() => this.toast.onSuccess("Successfully saved batch item")),
                    catchError(err => {
                        this.toast.onError("Unable to save batch item");
                        return throwError(() => err);
                    })
                );
        }
        else{
            const updateRequest = {
                batchName: batch.batchName,
                cronExpression: batch.cronExpression,
                jobs: (batch.jobs ?? []).map((j: any) => j.jobId),
                emails: batch.emails ?? [],
                groupIds: batch.groupIds ?? []
            };
            return this.httpInterceptService.put<Batch>(`${environment.apiUrl}/batches/${batch.id}`, updateRequest)
                .pipe(
                    tap(() => this.toast.onSuccess("Successfully saved batch item")),
                    catchError(err => {
                        this.toast.onError("Unable to save batch item");
                        return throwError(() => err);
                    })
                );
        }

    }

}