import {inject, Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {Batch} from "../../models/batch.model";
import {catchError, Observable, tap, throwError} from "rxjs";
import {ToastService} from "../../services/toast.service";
import {environment} from "../../../environment";


@Injectable({ providedIn: 'root' })
export class BatchApi {
    constructor(private http: HttpClient) {}
    private baseUrl = '/api/batches';
    private toast = inject(ToastService);

    getAllBatches(): Observable<Batch[]>{
        return this.http.get<Batch[]>(`${environment.apiUrl}`);
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