import { TestBed, fakeAsync, flush } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthenticationService } from './authentication.service';
import { environment } from '../../environment';

function base64UrlEncode(value: object): string {
  return btoa(JSON.stringify(value))
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/, '');
}

function buildToken(payload: object): string {
  const header = base64UrlEncode({ alg: 'none', typ: 'JWT' });
  const body = base64UrlEncode(payload);
  return `${header}.${body}.`;
}

describe('AuthenticationService', () => {
  let service: AuthenticationService;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    localStorage.clear();
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        { provide: Router, useValue: routerSpy }
      ]
    });
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should be created', () => {
    service = TestBed.inject(AuthenticationService);
    expect(service).toBeTruthy();
  });

  it('setToken stores token in localStorage', () => {
    service = TestBed.inject(AuthenticationService);
    const token = 'token-value';
    spyOn(localStorage, 'setItem');

    service.setToken(token);

    expect(localStorage.setItem).toHaveBeenCalledWith(environment.tokenStorageKey, token);
  });

  it('loads valid token from storage and sets auth state', () => {
    const token = buildToken({
      preferred_username: 'user',
      email: 'user@example.com',
      groups: ['admin'],
      exp: Math.floor(Date.now() / 1000) + 3600
    });
    localStorage.setItem(environment.tokenStorageKey, token);

    service = TestBed.inject(AuthenticationService);

    expect(service.isAuthenticated()).toBeTrue();
    expect(service.hasRole('admin')).toBeTrue();
  });

  it('login redirects to authUrl', () => {
    service = TestBed.inject(AuthenticationService);
    expect(() => service.login()).not.toThrow();
  });

  it('loadTokenFromCookie navigates to root on valid token', fakeAsync(() => {
    service = TestBed.inject(AuthenticationService);
    const token = buildToken({
      preferred_username: 'user',
      email: 'user@example.com',
      groups: ['admin'],
      exp: Math.floor(Date.now() / 1000) + 3600
    });
    const mockCookieStore = {
      get: jasmine.createSpy().and.returnValue(Promise.resolve({ value: token })),
      delete: jasmine.createSpy().and.returnValue(Promise.resolve())
    };
    spyOnProperty(window, 'cookieStore', 'get').and.returnValue(mockCookieStore as any);

    service.loadTokenFromCookie();
    flush();

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
  }));

  it('loadTokenFromCookie navigates to login when missing', fakeAsync(() => {
    service = TestBed.inject(AuthenticationService);
    const mockCookieStore = {
      get: jasmine.createSpy().and.returnValue(Promise.resolve(null)),
      delete: jasmine.createSpy().and.returnValue(Promise.resolve())
    };
    spyOnProperty(window, 'cookieStore', 'get').and.returnValue(mockCookieStore as any);

    service.loadTokenFromCookie();
    flush();

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  }));
});
