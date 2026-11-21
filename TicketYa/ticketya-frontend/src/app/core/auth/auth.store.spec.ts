import { TestBed } from '@angular/core/testing';
import { AuthStore } from './auth.store';

describe('AuthStore', () => {
  let store: AuthStore;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    store = TestBed.inject(AuthStore);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should save the authenticated session', () => {
    store.setSession({
      idUsuario: 'cliente-01',
      rol: 'CLIENTE',
      token: 'token-prueba'
    });

    expect(store.isAuthenticated()).toBe(true);
    expect(store.idUsuario()).toBe('cliente-01');
    expect(store.rol()).toBe('CLIENTE');
    expect(localStorage.getItem('ticketya.auth')).toContain('token-prueba');
  });

  it('should clear the session', () => {
    store.setSession({
      idUsuario: 'cliente-01',
      rol: 'CLIENTE',
      token: 'token-prueba'
    });

    store.clearSession();

    expect(store.isAuthenticated()).toBe(false);
    expect(store.token()).toBeNull();
    expect(localStorage.getItem('ticketya.auth')).toBeNull();
  });
});
