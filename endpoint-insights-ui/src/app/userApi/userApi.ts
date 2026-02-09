import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {HttpClient, HttpParams} from "@angular/common/http";
import {environment} from "../../environment";


@Injectable({ providedIn: 'root' })
export class UserApi {
    constructor(private http: HttpClient) {}
    private baseUrl = '/api/users';

    getAllUsersByQuery(query: string): Observable<User[]>{
        const params = {
            query: query
        }
        return this.http.get<User[]>(`${environment.apiUrl}/${this.baseUrl}/matching-query`, { params });
    }

    getAllUsersByIds(idList: string[]){
        const params = {
            idList: idList
        }

        return this.http.get<User[]>(`${environment.apiUrl}/${this.baseUrl}/find-by-ids`, { params });
    }

}