import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Router} from '@angular/router';
import {BehaviorSubject, catchError, map, Observable, of} from 'rxjs';
import {environment} from "../../environment";

interface UserInfo {
  expiresAt: number;
  username: string;
  email: string;
  roles: string[];
}

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  private token: string | null = null;
  private readonly authUrl = environment.authUrl;
  private readonly apiUrl = environment.apiUrl;
  private readonly tokenKey = environment.tokenStorageKey;

  private userInfo: UserInfo | null = null;

  // Observable to track authentication state
  private authStateSubject = new BehaviorSubject<boolean>(false);
  public authState$ = this.authStateSubject.asObservable();

  constructor(
    private httpClient: HttpClient,
    private router: Router
  ) {
    this.loadTokenFromStorage();
    if (this.token) {
      this.loadUserInfo();
    }
  }

  /**
   * Initiates the OAuth2 authentication flow
   * Stores current route for post-auth redirect
   */
  public login(): void {
    window.location.href = this.authUrl;
  }

  public getUserInfo(): UserInfo {
    return this.userInfo!;
  }

  public loadUserInfo() {
    if (this.userInfo)
      return;

   this.httpClient.get<UserInfo>(`${this.apiUrl}/auth/user-info`, {
      headers: new HttpHeaders().set('Authorization', `Bearer ${this.getToken()}`),
    }).pipe(
        catchError(error => {
          console.error(error);
          throw error;
        })
    ).subscribe(userInfo => {
      this.userInfo = userInfo;
   })
  }

  /**
   * Checks if the user's roles contains role. (CaSe SeNsItIvE)
   * */
  public hasRole(role: string): boolean {
    if (this.token) {
      console.log("expiration left: " + this.isTokenExpired());
    }
    return this.isAuthenticated() && this.userInfo! && this.getUserInfo().roles.includes(role);
  }

  /**
   * Logs out the user and clears authentication data
   */
  public logout(): void {
    this.clearAuthData();
    this.authStateSubject.next(false);
    
    this.router.navigate(['/']);
  }

  /**
   * Checks if user is authenticated
   */
  public isAuthenticated(): boolean {
    return this.token != null && !this.isTokenExpired();
  }

  /**
   * Checks if the current token is expired
   */
  private isTokenExpired(): boolean {
    if (!this.token) return true;
    
    try {
      const payload = JSON.parse(atob(this.token.split('.')[1]));
      const currentTime = Math.floor(Date.now() / 1000);
      return payload.exp < currentTime;
    } catch {
      return true;
    }
  }

  /**
   * Gets the current auth token
   */
  getToken(): string | null {
    return this.token;
  }

  /**
   * Sets the authentication token
   */
  public setToken(token: string): void {
    this.token = token;
    localStorage.setItem(this.tokenKey, token);
  }

  /**
   * Loads token from local storage
   */
  private loadTokenFromStorage(): void {
    const token = localStorage.getItem(this.tokenKey);
    if (token && !this.isTokenExpired()) {
      this.token = token;
      this.authStateSubject.next(true);
    } else if (token) {
      // Token exists but is expired
      this.clearAuthData();
    }
  }

  /**
   * Clears all authentication data
   */
  private clearAuthData(): void {
    this.token = null;
    localStorage.removeItem(this.tokenKey);
  }

  // API methods using the authenticated token
  getHealthCheck(): Observable<string> {
    return this.httpClient.get<string>(`${this.apiUrl}/api/health`);
  }

  getSecureHealthCheck(): Observable<string> {
    return this.httpClient.get<string>(`${this.apiUrl}/api/health-secure`);
  }
}