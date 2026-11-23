import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { AuthStore } from './auth.store';

export const authErrorInterceptor: HttpInterceptorFn = (request, next) => {
  const authStore = inject(AuthStore);

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      const isLoginRequest = request.url.endsWith('/clientes/login');

      // Un 401 en una operacion privada significa que la sesion ya no es valida
      // En el login, en cambio, un 401 solo significa credenciales incorrectas
      if (error.status === 401 && !isLoginRequest) {
        authStore.clearSession();
      }

      // Un 403 NO elimina la sesion: el usuario esta autenticado
      // pero no tiene permisos para esa operacion
      return throwError(() => error);
    })
  );
};
