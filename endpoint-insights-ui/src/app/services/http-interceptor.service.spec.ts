import { TestBed } from '@angular/core/testing';
import { HttpHeaders } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BehaviorSubject } from 'rxjs';
import { AuthenticationService } from './authentication.service';
import { HttpInterceptorService } from './http-interceptor.service';

class AuthenticationServiceStub {
  authState$ = new BehaviorSubject<boolean>(false);
  getToken() {
    return 'test-token';
  }
}

describe('HttpInterceptorService', () => {
  let service: HttpInterceptorService;
  let authStub: AuthenticationServiceStub;

  beforeEach(() => {
    authStub = new AuthenticationServiceStub();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        HttpInterceptorService,
        { provide: AuthenticationService, useValue: authStub }
      ]
    });
    service = TestBed.inject(HttpInterceptorService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('injectAuthenticationToken adds Authorization header when token present', () => {
    authStub.authState$.next(true);

    const headers = (service as any).injectAuthenticationToken(new HttpHeaders());

    expect(headers.get('Authorization')).toBe('Bearer test-token');
  });

  it('injectAuthenticationToken keeps headers when no token', () => {
    authStub.authState$.next(false);

    const headers = (service as any).injectAuthenticationToken(new HttpHeaders());

    expect(headers.has('Authorization')).toBeFalse();
  });
});
