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
    const redirectSpy = spyOn<any>(service, 'redirectToAuth');

    service.login();

    expect(redirectSpy).toHaveBeenCalledWith(environment.authUrl);
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

  it('loadTokenFromCookie navigates to stored redirect URL on valid token', fakeAsync(() => {
    service = TestBed.inject(AuthenticationService);
    const token = buildToken({
      preferred_username: 'user',
      email: 'user@example.com',
      groups: ['admin'],
      exp: Math.floor(Date.now() / 1000) + 3600
    });
    //Store redirect URL before loading token
    localStorage.setItem('redirectUrl', '/batches');
    const mockCookieStore = {
      get: jasmine.createSpy().and.returnValue(Promise.resolve({ value: token })),
      delete: jasmine.createSpy().and.returnValue(Promise.resolve())
    };
    spyOnProperty(window, 'cookieStore', 'get').and.returnValue(mockCookieStore as any);
    service.loadTokenFromCookie();
    flush();
    //Verify that user is redirected to stored URL and redirect URL is cleared from storage
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/batches']);
    expect(localStorage.getItem('redirectUrl')).toBeNull();
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

  describe('Redirect URL Security', () => {
    beforeEach(() => {
      service = TestBed.inject(AuthenticationService);
    });

    it('setRedirectUrl stores valid safe URL in localStorage', () => {
      spyOn(localStorage, 'setItem');
      service.setRedirectUrl('/batches');
      expect(localStorage.setItem).toHaveBeenCalledWith('redirectUrl', '/batches');
    });

    it('setRedirectUrl rejects invalid redirect URLs and logs warning', () => {
      spyOn(console, 'warn');
      spyOn(localStorage, 'setItem');
      
      service.setRedirectUrl('https://evil.com/phishing');
      
      expect(localStorage.setItem).not.toHaveBeenCalled();
      expect(console.warn).toHaveBeenCalled();
    });

    it('setRedirectUrl rejects data: protocol URLs', () => {
      spyOn(console, 'warn');
      spyOn(localStorage, 'setItem');
      
      service.setRedirectUrl('data:text/html,<script>alert("xss")</script>');
      
      expect(localStorage.setItem).not.toHaveBeenCalled();
      expect(console.warn).toHaveBeenCalled();
    });

    it('getAndClearRedirectUrl retrieves and removes valid URL from localStorage', () => {
      localStorage.setItem('redirectUrl', '/batches');
      spyOn(localStorage, 'removeItem');
      
      const result = service.getAndClearRedirectUrl();
      
      expect(result).toBe('/batches');
      expect(localStorage.removeItem).toHaveBeenCalledWith('redirectUrl');
    });

    it('getAndClearRedirectUrl returns null when no URL is stored', () => {
      const result = service.getAndClearRedirectUrl();
      expect(result).toBeNull();
    });

    it('getAndClearRedirectUrl validates stored URL and logs warning if invalid', () => {
      spyOn(console, 'warn');
      // Manually inject an invalid URL to simulate tampering
      localStorage.setItem('redirectUrl', 'https://evil.com/phishing');
      
      const result = service.getAndClearRedirectUrl();
      
      expect(result).toBeNull();
      expect(console.warn).toHaveBeenCalled();
    });

    it('loadTokenFromCookie does not redirect to invalid stored URL', fakeAsync(() => {
      const token = buildToken({
        preferred_username: 'user',
        email: 'user@example.com',
        groups: ['admin'],
        exp: Math.floor(Date.now() / 1000) + 3600
      });
      // Manually inject invalid URL
      localStorage.setItem('redirectUrl', 'https://evil.com/phishing');
      const mockCookieStore = {
        get: jasmine.createSpy().and.returnValue(Promise.resolve({ value: token })),
        delete: jasmine.createSpy().and.returnValue(Promise.resolve())
      };
      spyOnProperty(window, 'cookieStore', 'get').and.returnValue(mockCookieStore as any);
      
      service.loadTokenFromCookie();
      flush();
      
      // Should navigate to default '/' instead of evil.com
      expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
      expect(localStorage.getItem('redirectUrl')).toBeNull();
    }));
  });

  describe('URL Validation (isValidRedirectUrl)', () => {
    beforeEach(() => {
      service = TestBed.inject(AuthenticationService);
    });

    describe('Safe URLs', () => {
      it('should allow absolute path /dashboard', () => {
        // Using the service to validate via its redirect methods
        const consoleSpy = spyOn(console, 'warn');
        service.setRedirectUrl('/dashboard');
        const stored = localStorage.getItem('redirectUrl');
        expect(stored).toBe('/dashboard');
        expect(consoleSpy).not.toHaveBeenCalled();
      });

      it('should allow nested absolute path /profile/settings', () => {
        const consoleSpy = spyOn(console, 'warn');
        service.setRedirectUrl('/profile/settings');
        const stored = localStorage.getItem('redirectUrl');
        expect(stored).toBe('/profile/settings');
        expect(consoleSpy).not.toHaveBeenCalled();
      });

      it('should allow relative path ./relative/path', () => {
        const consoleSpy = spyOn(console, 'warn');
        service.setRedirectUrl('./relative/path');
        const stored = localStorage.getItem('redirectUrl');
        expect(stored).toBe('./relative/path');
        expect(consoleSpy).not.toHaveBeenCalled();
      });

      it('should allow parent relative path ../parent/path', () => {
        const consoleSpy = spyOn(console, 'warn');
        service.setRedirectUrl('../parent/path');
        const stored = localStorage.getItem('redirectUrl');
        expect(stored).toBe('../parent/path');
        expect(consoleSpy).not.toHaveBeenCalled();
      });

      it('should allow URL with query parameters /page?param=value&other=123', () => {
        const consoleSpy = spyOn(console, 'warn');
        service.setRedirectUrl('/page?param=value&other=123');
        const stored = localStorage.getItem('redirectUrl');
        expect(stored).toBe('/page?param=value&other=123');
        expect(consoleSpy).not.toHaveBeenCalled();
      });

      it('should allow URL with fragment /page#section', () => {
        const consoleSpy = spyOn(console, 'warn');
        service.setRedirectUrl('/page#section');
        const stored = localStorage.getItem('redirectUrl');
        expect(stored).toBe('/page#section');
        expect(consoleSpy).not.toHaveBeenCalled();
      });

      it('should allow http: protocol with same origin', () => {
        const consoleSpy = spyOn(console, 'warn');
        const currentOrigin = new URL(window.location.href).origin;
        service.setRedirectUrl(`${currentOrigin}/dashboard`);
        const stored = localStorage.getItem('redirectUrl');
        expect(stored).toBe(`${currentOrigin}/dashboard`);
        expect(consoleSpy).not.toHaveBeenCalled();
      });

      it('should allow https: protocol with same origin', () => {
        const consoleSpy = spyOn(console, 'warn');
        const currentOrigin = new URL(window.location.href).origin;
        service.setRedirectUrl(`${currentOrigin}/dashboard`);
        const stored = localStorage.getItem('redirectUrl');
        expect(stored).toBe(`${currentOrigin}/dashboard`);
        expect(consoleSpy).not.toHaveBeenCalled();
      });
    });

    describe('Special Characters in Paths', () => {
      it('should allow URLs with special characters in query', () => {
        const consoleSpy = spyOn(console, 'warn');
        service.setRedirectUrl('/search?q=hello%20world');
        const stored = localStorage.getItem('redirectUrl');
        expect(stored).toBe('/search?q=hello%20world');
        expect(consoleSpy).not.toHaveBeenCalled();
      });

      it('should allow URLs with multiple query params', () => {
        const consoleSpy = spyOn(console, 'warn');
        service.setRedirectUrl('/page?a=1&b=2&c=3');
        const stored = localStorage.getItem('redirectUrl');
        expect(stored).toBe('/page?a=1&b=2&c=3');
        expect(consoleSpy).not.toHaveBeenCalled();
      });
    });

    describe('Unsafe URLs - Dangerous Protocols', () => {
      it('should reject javascript: protocol', () => {
        const consoleSpy = spyOn(console, 'warn');
        service.setRedirectUrl('javascript:alert("xss")');
        const stored = localStorage.getItem('redirectUrl');
        expect(stored).toBeNull();
        expect(consoleSpy).toHaveBeenCalled();
      });

      it('should reject data: protocol', () => {
        const consoleSpy = spyOn(console, 'warn');
        service.setRedirectUrl('data:text/html,<script>alert("xss")</script>');
        const stored = localStorage.getItem('redirectUrl');
        expect(stored).toBeNull();
        expect(consoleSpy).toHaveBeenCalled();
      });

      it('should reject vbscript: protocol', () => {
        const consoleSpy = spyOn(console, 'warn');
        service.setRedirectUrl('vbscript:alert("xss")');
        const stored = localStorage.getItem('redirectUrl');
        expect(stored).toBeNull();
        expect(consoleSpy).toHaveBeenCalled();
      });

      it('should reject mailto: protocol', () => {
        const consoleSpy = spyOn(console, 'warn');
        service.setRedirectUrl('mailto:attacker@evil.com');
        const stored = localStorage.getItem('redirectUrl');
        expect(stored).toBeNull();
        expect(consoleSpy).toHaveBeenCalled();
      });

      it('should reject tel: protocol', () => {
        const consoleSpy = spyOn(console, 'warn');
        service.setRedirectUrl('tel:123');
        const stored = localStorage.getItem('redirectUrl');
        expect(stored).toBeNull();
        expect(consoleSpy).toHaveBeenCalled();
      });

      it('should reject file: protocol', () => {
        const consoleSpy = spyOn(console, 'warn');
        service.setRedirectUrl('file:///etc/passwordd');
        const stored = localStorage.getItem('redirectUrl');
        expect(stored).toBeNull();
        expect(consoleSpy).toHaveBeenCalled();
      });

      it('should reject ftp: protocol', () => {
        const consoleSpy = spyOn(console, 'warn');
        service.setRedirectUrl('ftp://evil.com/file');
        const stored = localStorage.getItem('redirectUrl');
        expect(stored).toBeNull();
        expect(consoleSpy).toHaveBeenCalled();
      });

      it('should reject javascript: with mixed case', () => {
        const consoleSpy = spyOn(console, 'warn');
        service.setRedirectUrl('JavaScript:alert("xss")');
        const stored = localStorage.getItem('redirectUrl');
        expect(stored).toBeNull();
        expect(consoleSpy).toHaveBeenCalled();
      });

      it('should reject http: protocol with different origin', () => {
        const consoleSpy = spyOn(console, 'warn');
        service.setRedirectUrl('http://evil.com/phishing');
        const stored = localStorage.getItem('redirectUrl');
        expect(stored).toBeNull();
        expect(consoleSpy).toHaveBeenCalled();
      });

      it('should reject https: protocol with different origin', () => {
        const consoleSpy = spyOn(console, 'warn');
        service.setRedirectUrl('https://evil.com/phishing');
        const stored = localStorage.getItem('redirectUrl');
        expect(stored).toBeNull();
        expect(consoleSpy).toHaveBeenCalled();
      });
    });

    describe('Edge Cases', () => {
      it('should reject empty string', () => {
        service.setRedirectUrl('');
        const stored = localStorage.getItem('redirectUrl');
        // Empty string should not be stored
        expect(stored).toBeNull();
      });

      it('should reject whitespace-only string', () => {
        service.setRedirectUrl('   ');
        const stored = localStorage.getItem('redirectUrl');
        // Whitespace-only should not be stored
        expect(stored).toBeNull();
      });
    });
  });
});
