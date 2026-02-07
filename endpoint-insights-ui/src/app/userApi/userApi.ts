import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {HttpClient, HttpParams} from "@angular/common/http";


@Injectable({ providedIn: 'root' })
export class UserApi {
    constructor(private http: HttpClient) {}
    private baseUrl = 'http://localhost:8080/api/users';

    getAllUsersByQuery(query: string): Observable<User[]>{
        const params = {
            query: query
        }
        return this.http.get<User[]>(`${this.baseUrl}/matching-query`, { params });
    }

    getAllUsersByIds(idList: string[]){
        const params = {
            idList: idList
        }

        return this.http.get<User[]>(`${this.baseUrl}/find-by-ids`, { params });
    }

}