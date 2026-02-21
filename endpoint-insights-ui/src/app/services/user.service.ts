import {inject, Injectable} from "@angular/core";
import {Observable, of} from "rxjs";
import {UserApi} from "../userApi/userApi";


@Injectable({ providedIn: 'root' })
export class UserService {
    constructor() {}
    private userApi = inject(UserApi);

    searchUsers(query: string | null):Observable<User[]>{
        if(query === null || query === "") return of([]);
        return this.userApi.getAllUsersByQuery(query);
    }

    findUsersById(list: string[]){
        return this.userApi.getAllUsersByIds(list);
    }
}