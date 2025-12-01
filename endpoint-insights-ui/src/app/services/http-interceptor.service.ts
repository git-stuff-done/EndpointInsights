import { Injectable } from '@angular/core';
import {AuthenticationService} from "./authentication.service";
import {HttpClient, HttpHeaders, HttpResponse} from "@angular/common/http";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class HttpInterceptorService {

  private authenticationToken: string|null = null;

  constructor(private authenticationService: AuthenticationService, private httpClient: HttpClient) {
    this.authenticationService.authState$.subscribe(authState => {
      this.authenticationToken = authState ? this.authenticationService.getToken() : null;
    });
  }

  get<T>(url: string, headers?: HttpHeaders) : Observable<HttpResponse<T>> {
    this.injectAuthenticationToken(headers);
    return this.httpClient.get<T>(url, {headers: headers, observe: 'response'});
  }

  post<T>(url: string, body: any, headers?: HttpHeaders) : Observable<HttpResponse<T>> {
    this.injectAuthenticationToken(headers);
    return this.httpClient.post<T>(url, body, {headers: headers, observe: 'response'});
  }

  put<T>(url: string, body: any, headers?: HttpHeaders) : Observable<HttpResponse<T>> {
    this.injectAuthenticationToken(headers);
    return this.httpClient.put<T>(url, body, {headers: headers, observe: 'response'});
  }

  delete<T>(url: string, headers?: HttpHeaders) : Observable<HttpResponse<T>> {
    this.injectAuthenticationToken(headers);
    return this.httpClient.delete<T>(url, {headers: headers, observe: 'response'});
  }

  options<T>(url: string, headers?: HttpHeaders) : Observable<HttpResponse<T>> {
    this.injectAuthenticationToken(headers);
    return this.httpClient.options<T>(url, {headers: headers, observe: 'response'});
  }

  private injectAuthenticationToken(headers?: HttpHeaders) : HttpHeaders {
    if (headers == null)
      headers = new HttpHeaders();

    if (this.authenticationToken) {
      return headers.set('Authorization', `Bearer ${this.authenticationToken}`);
    } else {
      return headers;
    }
  }

}
