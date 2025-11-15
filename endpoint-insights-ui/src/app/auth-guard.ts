import {CanActivateFn, Router} from '@angular/router';
import {AuthenticationService} from "./services/authentication.service";
import {inject} from "@angular/core";

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthenticationService);
  const router = inject(Router);
  authService.authState$.subscribe(next => {
    if (!next) {
      router.navigate(['/login']);
    }
  });

  return true;
};
