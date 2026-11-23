import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { AuthStore } from './auth.store';

const LOGIN_RESPONSE = {
  idUsuario: 'cliente-01',
  rol: 'CLIENTE' as const,
  token: 'token-prueba'
};

describe('AuthService', () => {
  let service: AuthService;
  let authStore: AuthStore;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(AuthService);
    authStore = TestBed.inject(AuthStore);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.clear();
  });

  it('should login and save the session', () => {
    service.login({ idUsuario: 'cliente-01', password: 'clave' }).subscribe();

    const request = httpTesting.expectOne('/clientes/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      idUsuario: 'cliente-01',
      password: 'clave'
    });

    request.flush(LOGIN_RESPONSE);

    expect(authStore.idUsuario()).toBe('cliente-01');
    expect(authStore.rol()).toBe('CLIENTE');
    expect(authStore.token()).toBe('token-prueba');
  });
});
