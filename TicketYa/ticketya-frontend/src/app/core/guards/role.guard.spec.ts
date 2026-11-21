import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
  provideRouter
} from '@angular/router';
import { AuthStore } from '../auth/auth.store';
import { roleGuard } from './role.guard';

describe('roleGuard', () => {
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

  it('should allow a valid role', () => {
    store.setSession({
      idUsuario: 'admin',
      rol: 'ADMINISTRADOR',
      token: 'token-prueba'
    });

    const guard = roleGuard('ADMINISTRADOR');
    const result = TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, { url: '/admin' } as RouterStateSnapshot)
    );

    expect(result).toBe(true);
  });

  it('should redirect a user with the wrong role', () => {
    store.setSession({
      idUsuario: 'cliente-01',
      rol: 'CLIENTE',
      token: 'token-prueba'
    });

    const guard = roleGuard('ADMINISTRADOR');
    const result = TestBed.runInInjectionContext(() =>
      guard({} as ActivatedRouteSnapshot, { url: '/admin' } as RouterStateSnapshot)
    );

    expect(router.serializeUrl(result as ReturnType<Router['createUrlTree']>)).toBe('/');
  });
});
