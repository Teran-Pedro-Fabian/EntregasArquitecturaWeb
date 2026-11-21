import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
  provideRouter
} from '@angular/router';
import { AuthStore } from '../auth/auth.store';
import { authGuard } from './auth.guard';

describe('authGuard', () => {
  let store: AuthStore;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [provideRouter([])]
    });

    store = TestBed.inject(AuthStore);
    router = TestBed.inject(Router);
  });

  afterEach(() => localStorage.clear());

  it('should allow an authenticated user', () => {
    store.setSession({
      idUsuario: 'cliente-01',
      rol: 'CLIENTE',
      token: 'token-prueba'
    });

    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, { url: '/mis-entradas' } as RouterStateSnapshot)
    );

    expect(result).toBe(true);
  });

  it('should redirect an unauthenticated user to login', () => {
    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, { url: '/mis-entradas' } as RouterStateSnapshot)
    );

    expect(router.serializeUrl(result as ReturnType<Router['createUrlTree']>)).toBe(
      '/login?returnUrl=%2Fmis-entradas'
    );
  });
});
