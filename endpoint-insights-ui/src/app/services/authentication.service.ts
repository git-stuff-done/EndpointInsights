import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Router} from '@angular/router';
import {BehaviorSubject, Observable} from 'rxjs';
import {environment} from "../../environment";
import {jwtDecode} from "jwt-decode";

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
  private readonly AUTH_TOKEN_COOKIE = "authToken";


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

    const decodedToken = jwtDecode(this.token!) as any;

    if (decodedToken) {
      this.userInfo = {
        username: decodedToken.preferred_username,
        email: decodedToken.email,
        roles: decodedToken.groups,
        expiresAt: decodedToken.exp
      } as UserInfo;
    }
  }

  /**
   * Checks if the user's roles contains role. (CaSe SeNsItIvE)
   * */
  public hasRole(role: string): boolean {
    return this.isAuthenticated() && this.userInfo! && this.getUserInfo().roles.includes(role);
  }

  /**
   * Logs out the user and clears authentication data
   */
  public logout(): void {
    this.clearAuthData();
    this.authStateSubject.next(false);

    this.router.navigate(['/login']);
  }

  /**
   * Checks if user is authenticated
   */
  public isAuthenticated(): boolean {

      return this.authStateSubject.value;
  }

  /**
   * Checks if the current token is expired
   */
  private isTokenExpired(token: any): boolean {
    if (!token) return true;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
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

  public loadTokenFromCookie() {
    const token = window.cookieStore.get(this.AUTH_TOKEN_COOKIE);
    token.then(t => {
      if (t == null) {
          this.router.navigate(['/']);
      } else if (t?.value) {
        window.cookieStore.delete(this.AUTH_TOKEN_COOKIE);
        this.setToken(t.value);
        this.loadTokenFromStorage();
        this.loadUserInfo();
        this.router.navigate(['/']);
      }

    }).catch(e => console.log(e));
  }

  /**
   * Loads token from local storage
   */
  private loadTokenFromStorage(): void {
    const token = localStorage.getItem(this.tokenKey);
    if (token && !this.isTokenExpired(token)) {
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
    window.cookieStore.delete(this.AUTH_TOKEN_COOKIE);
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