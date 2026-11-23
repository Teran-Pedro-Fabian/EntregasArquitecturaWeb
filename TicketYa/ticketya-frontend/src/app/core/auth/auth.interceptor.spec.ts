import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { authInterceptor } from './auth.interceptor';
import { AuthStore } from './auth.store';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpTesting: HttpTestingController;
  let authStore: AuthStore;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting()
      ]
    });

    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
    authStore = TestBed.inject(AuthStore);
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.clear();
  });

  it('should add the bearer token to private requests', () => {
    authStore.setSession({
      idUsuario: 'cliente-01',
      rol: 'CLIENTE',
      token: 'token-prueba'
    });

    http.get('/ticket/compras').subscribe();

    const request = httpTesting.expectOne('/ticket/compras');
    expect(request.request.headers.get('Authorization')).toBe('Bearer token-prueba');

    request.flush({});
  });

  it('should not add the token to login', () => {
    authStore.setSession({
      idUsuario: 'cliente-01',
      rol: 'CLIENTE',
      token: 'token-prueba'
    });

    http.post('/clientes/login', {}).subscribe();

    const request = httpTesting.expectOne('/clientes/login');
    expect(request.request.headers.has('Authorization')).toBe(false);

    request.flush({});
  });
});
