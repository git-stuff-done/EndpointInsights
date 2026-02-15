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
    private httpIntercept = inject(HttpInterceptorService);

    getAllBatches(): Observable<HttpResponse<Batch[]>>{
        return this.httpIntercept.get<Batch[]>(`${environment.apiUrl}/batches`)
    }

    getBatchById(id: string){
        return this.http.get<Batch>(`${environment.apiUrl}/${id}`)
    }

    saveBatch(batch: Batch):Observable<Batch>{
        return this.http.put<Batch>(`${environment.apiUrl}/${batch.id}`, batch)
            .pipe(
                tap(() => this.toast.onSuccess("Successfully saved batch item")),
                catchError(err => {
                    this.toast.onError("Unable to save batch item");
                    return throwError(() => err);
                })
            );
    }

}