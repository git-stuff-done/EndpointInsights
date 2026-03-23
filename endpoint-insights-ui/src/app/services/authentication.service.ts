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

/**
 * Validates if redirect URL is safe (same origin or relative path)
 * Prevents open redirect vulnerabilities by ensuring redirect URL doesn't send user to an external malicious site.
 * returns true if the redirect URL is safe
 * throws Error if redirect URL is invalid/malformed
 */
function isValidRedirectUrl(url: string): boolean {
  if (!url) {
    return false;
  }
  //Trim whitespace
  url = url.trim();
  if (!url) {
    return false;
  }
  //Check for dangerous protocols
  const dangerousProtocols = [ 'javascript:', 'vbscript:', 'ftp:', 'data:', 'file:', 'tel:', 'mailto:' ];
  const lowerUrl = url.toLowerCase();
  for (const protocol of dangerousProtocols) {
    if (lowerUrl.startsWith(protocol)) {
      return false;
    }
  }
  //Relative URLs starting with /, ./, ../, or no protocol are safe
  if ((url.startsWith('/')) || (url.startsWith('./')) || (url.startsWith('../'))) {
    return true;
  }
  //For absolute URLs, validate it has the same origin
  try {
    const urlObj = new URL(url, window.location.origin);
    const currentOrigin = new URL(window.location.href).origin;
    //Check if URL origin matches current origin
    if (urlObj.origin !== currentOrigin) {
      return false;
    }
    return true;
  } catch {
    //Invalid URL throws error
    throw new Error(`Invalid URL format: ${url}`);
  }
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
  private readonly REDIRECT_URL_KEY = "redirectUrl";


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
    this.redirectToAuth(this.authUrl);
  }

  /**
   * Stores the URL the user attempted to visit before redirect
   * Only stores URLs that are safe to redirect to (same origin or relative paths)
   * Rejects open redirect attempts and logs security warnings
   */
  public setRedirectUrl(url: string): void {
    try {
      if (isValidRedirectUrl(url)) {
        localStorage.setItem(this.REDIRECT_URL_KEY, url);
      } else {
        console.warn(`[SECURITY] Blocked attempt to store invalid redirect URL: ${url}`);
      }
    } catch (error) {
      console.error(`[SECURITY] Error validating redirect URL: ${error}`);
    }
  }

  /**
   * Retrieves the redirect URL and THEN clears it from storage.
   * Validates stored URL is safe before returning.
   * Returns URL if stored and valid
   * Returns null if no URL is stored or if stored URL is invalid
   */
  public getAndClearRedirectUrl(): string | null {
    const url = localStorage.getItem(this.REDIRECT_URL_KEY);
    if (url) {
      localStorage.removeItem(this.REDIRECT_URL_KEY);
      //Check stored URL is still valid before returning
      try {
        if (isValidRedirectUrl(url)) {
          return url;
        } else {
          console.warn(`[SECURITY] Stored redirect URL is invalid: ${url}`);
          return null;
        }
      } catch (error) {
        console.error(`[SECURITY] Error validating stored redirect URL: ${error}`);
        return null;
      }
    }
    return null;
  }

  private redirectToAuth(url: string): void {
    window.location.assign(url);
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
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const payload = JSON.parse(atob(base64));
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
          this.router.navigate(['/login']);
      } else if (t?.value) {
        window.cookieStore.delete(this.AUTH_TOKEN_COOKIE);
        if (!this.isTokenExpired(t.value)) {
          this.setToken(t.value);
          this.authStateSubject.next(true);
          this.loadUserInfo();
          //Redirect to the stored URL or root if no URL is stored
          const redirectUrl = this.getAndClearRedirectUrl();
          this.router.navigate([redirectUrl || '/']);
        } else {
          this.clearAuthData();
          this.router.navigate(['/login']);
        }
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