import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { UserRole } from '../auth/auth.models';
import { AuthStore } from '../auth/auth.store';

export const roleGuard = (...allowedRoles: UserRole[]): CanActivateFn => {
  return (_route, state) => {
    const authStore = inject(AuthStore);
    const router = inject(Router);

    // no depende del authGuard porque Angular puede ejecutar guards juntos
    if (!authStore.isAuthenticated()) {
      return router.createUrlTree(['/login'], {
        queryParams: { returnUrl: state.url }
      });
    }

    const rol = authStore.rol();

    if (rol && allowedRoles.includes(rol)) {
      return true;
    }

    return router.createUrlTree(['/']);
  };
};
