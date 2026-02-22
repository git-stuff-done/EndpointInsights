import {CanActivateFn, Router} from '@angular/router';
import {AuthenticationService} from "./services/authentication.service";
import {inject} from "@angular/core";

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthenticationService);
  const router = inject(Router);

//   if (!authService.isAuthenticated()) {
//     router.navigate(['/login']);
//     return false;
//   }

  return true;
};
