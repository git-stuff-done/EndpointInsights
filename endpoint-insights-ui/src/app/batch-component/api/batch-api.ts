import {inject, Injectable} from "@angular/core";
import {HttpClient, HttpParams} from "@angular/common/http";
import {Batch} from "../../models/batch.model";
import {catchError, Observable, tap, throwError} from "rxjs";
import {ToastService} from "../../services/toast.service";


@Injectable({ providedIn: 'root' })
export class BatchApi {
    constructor(private http: HttpClient) {}
    private baseUrl = 'http://localhost:8080/api/batches';
    private toast = inject(ToastService);

    getAllBatches(): Observable<Batch[]>{
        return this.http.get<Batch[]>(`${this.baseUrl}`);
    }

    getBatchById(id: string){
        return this.http.get<Batch>(`/api/batches/${id}`)
    }

    saveBatch(batch: Batch):Observable<Batch>{
        return this.http.put<Batch>(`${this.baseUrl}/update`, batch)
            .pipe(
                tap(() => this.toast.onSuccess("Successfully saved batch item")),
                catchError(err => {
                    this.toast.onError("Unable to save batch item");
                    return throwError(() => err);
                })
            );
    }

}