import {inject, Injectable} from "@angular/core";
import {HttpClient, HttpResponse} from "@angular/common/http";
import {Batch} from "../../models/batch.model";
import {catchError, Observable, tap, throwError} from "rxjs";
import {ToastService} from "../../services/toast.service";
import {environment} from "../../../environment";
import {HttpInterceptorService} from "../../services/http-interceptor.service";


@Injectable({ providedIn: 'root' })
export class BatchApi {
    constructor(private http: HttpClient) {}
    private toast = inject(ToastService);
    private httpInterceptService = inject(HttpInterceptorService);

    getAllBatches(): Observable<Batch[]>{
        return this.http.get<Batch[]>(`${environment.apiUrl}`);
    }

    getBatchById(id: string){
        return this.http.get<Batch>(`${environment.apiUrl}/${id}`)
    }

    saveBatch(batch: Batch):Observable<HttpResponse<Batch>>{
        if(batch.isNew){
            return this.httpInterceptService.post<Batch>(`${environment.apiUrl}/batches`, batch)
                .pipe(
                    tap(() => this.toast.onSuccess("Successfully saved batch item")),
                    catchError(err => {
                        this.toast.onError("Unable to save batch item");
                        return throwError(() => err);
                    })
                );
        }
        else{
            return this.httpInterceptService.put<Batch>(`${environment.apiUrl}/batches/${batch.id}`, batch)
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