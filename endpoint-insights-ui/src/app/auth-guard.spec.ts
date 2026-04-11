import { TestBed } from '@angular/core/testing';
import { CanActivateFn, Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { AuthenticationService } from './services/authentication.service';
import { authGuard } from './auth-guard';


describe('authGuard', () => {
  let authServiceSpy: jasmine.SpyObj<AuthenticationService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => authGuard(...guardParameters));

  beforeEach(() => {
    //Create spies for AuthenticationService and Router
    authServiceSpy = jasmine.createSpyObj('AuthenticationService', ['isAuthenticated', 'setRedirectUrl', 'getToken', 'hasRole', 'logout', 'login', 'loadUserInfo', 'getUserInfo'], {
      authState$: { subscribe: () => {} } as any
    });
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        { provide: AuthenticationService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    });
  });

  it('should be created', () => {
    //Verify that the guard can be executed without errors
    expect(executeGuard).toBeTruthy();
  });

  it('should allow access when user is authenticated', () => {
    authServiceSpy.isAuthenticated.and.returnValue(true);
    const result = executeGuard({ path: 'test' } as any, { url: '/batches' } as any);
    //Verify that access is granted and no redirection occurs
    expect(result).toBeTrue();
    expect(authServiceSpy.setRedirectUrl).not.toHaveBeenCalled();
  });

  it('should store redirect URL and navigate to login when user is not authenticated', () => {
    authServiceSpy.isAuthenticated.and.returnValue(false);
    const result = executeGuard({ path: 'batches' } as any, { url: '/batches' } as any);
    //Verify that access is denied, redirect URL is stored, and navigation to login occurs
    expect(result).toBeFalse();
    expect(authServiceSpy.setRedirectUrl).toHaveBeenCalledWith('/batches');
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  });
});
