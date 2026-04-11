import {CanActivateFn, Router} from '@angular/router';
import {AuthenticationService} from "./services/authentication.service";
import {inject} from "@angular/core";

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthenticationService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    //Store the destination before redirecting to login
    authService.setRedirectUrl(state.url);
    router.navigate(['/login']);
    return false;
  }

  return true;
};
