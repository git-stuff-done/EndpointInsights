import {Component, Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, tap, map } from 'rxjs/operators';
import {environment} from "../../environment";

interface AuthResponse {
  idToken: string;
  expiresAt: number;
  username: string;
  email: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {
  private token: string | null = null;
  private readonly authUrl = environment.authUrl;
  private readonly apiUrl = environment.apiUrl;
  private readonly tokenKey = environment.tokenStorageKey;
  private readonly redirectKey = 'auth_redirect_url';

  // Observable to track authentication state
  private authStateSubject = new BehaviorSubject<boolean>(false);
  public authState$ = this.authStateSubject.asObservable();

  constructor(
    private httpClient: HttpClient,
    private router: Router
  ) {
    this.loadTokenFromStorage();
    this.checkInitialAuthState();
  }

  /**
   * Initiates the OAuth2 authentication flow
   * Stores current route for post-auth redirect
   */
  login(returnUrl?: string): void {
    // Store the return URL for after authentication
    if (returnUrl) {
      localStorage.setItem(this.redirectKey, returnUrl);
    } else {
      // Store current route if no specific return URL provided
      localStorage.setItem(this.redirectKey, this.router.url);
    }

    window.location.href = this.authUrl;
  }

  /**
   * Handles the OAuth2 callback and processes the authentication result
   * Call this from your callback component or app initialization
   */
  handleAuthCallback(): Observable<boolean> {
    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');
    const state = urlParams.get('state');
    const error = urlParams.get('error');

    if (error) {
      console.error('OAuth2 authentication error:', error);
      this.clearAuthData();
      return of(false);
    }

    if (code && state) {
      // The OAuth2 flow should have already been processed by the backend
      // and we should be redirected back with the token in the response
      // Since your backend returns JSON after successful auth, you might need
      // to modify this based on how the backend handles the final redirect
      return this.verifyAuthenticationState();
    }

    return of(false);
  }

  /**
   * Verifies if the user is currently authenticated by checking with the backend
   */
  private verifyAuthenticationState(): Observable<boolean> {
    return this.httpClient.get<AuthResponse>(`${this.authUrl}/auth/verify`, {
      withCredentials: true // Important for session-based auth
    }).pipe(
      tap(response => {
        if (response && response.idToken) {
          this.setToken(response.idToken);
          this.authStateSubject.next(true);
        }
      }),
      map(response => {
        return !!(response && response.idToken);
      }),
      catchError(() => of(false))
    );
  }

  /**
   * Checks authentication state on app initialization
   */
  private checkInitialAuthState(): void {
    if (this.token) {
      // If we have a token, verify it's still valid
      this.verifyAuthenticationState().subscribe();
    } else {
      // Check if we're in an OAuth callback situation
      this.handleAuthCallback().subscribe();
    }
  }

  /**
   * Processes successful authentication and redirects user
   */
  handleSuccessfulAuth(authResponse: AuthResponse): void {
    this.setToken(authResponse.idToken);
    this.authStateSubject.next(true);
    
    // Get stored redirect URL and navigate there
    const redirectUrl = localStorage.getItem(this.redirectKey) || '/dashboard';
    localStorage.removeItem(this.redirectKey);
    
    this.router.navigate([redirectUrl]);
  }

  /**
   * Logs out the user and clears authentication data
   */
  logout(): void {
    this.clearAuthData();
    this.authStateSubject.next(false);
    
    this.router.navigate(['/']);
  }

  /**
   * Checks if user is authenticated
   */
  isAuthenticated(): boolean {
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
  private setToken(token: string): void {
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
    localStorage.removeItem(this.redirectKey);
  }

  // API methods using the authenticated token
  getHealthCheck(): Observable<string> {
    return this.httpClient.get<string>(`${this.apiUrl}/api/health`);
  }

  getSecureHealthCheck(): Observable<string> {
    return this.httpClient.get<string>(`${this.apiUrl}/api/health-secure`);
  }
}