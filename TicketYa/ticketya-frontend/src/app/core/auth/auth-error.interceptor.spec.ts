import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { authErrorInterceptor } from './auth-error.interceptor';
import { AuthStore } from './auth.store';

describe('authErrorInterceptor', () => {
  let http: HttpClient;
  let httpTesting: HttpTestingController;
  let authStore: AuthStore;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authErrorInterceptor])),
        provideHttpClientTesting()
      ]
    });

    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
    authStore = TestBed.inject(AuthStore);

    authStore.setSession({
      idUsuario: 'cliente-01',
      rol: 'CLIENTE',
      token: 'token-prueba'
    });
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.clear();
  });

  it('should clear the session after a 401 on a private request', () => {
    http.get('/ticket/compras').subscribe({ error: () => undefined });

    const request = httpTesting.expectOne('/ticket/compras');
    request.flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(authStore.isAuthenticated()).toBe(false);
  });

  it('should keep the session after a 403', () => {
    http.get('/ticket/reportes').subscribe({ error: () => undefined });

    const request = httpTesting.expectOne('/ticket/reportes');
    request.flush(null, { status: 403, statusText: 'Forbidden' });

    expect(authStore.isAuthenticated()).toBe(true);
  });
});
