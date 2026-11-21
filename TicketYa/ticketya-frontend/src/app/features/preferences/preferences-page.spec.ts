import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { AuthStore } from '../../core/auth/auth.store';
import { PreferencesPage } from './preferences-page';

describe('PreferencesPage', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PreferencesPage],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: AuthStore,
          useValue: {
            idUsuario: signal('cliente-01'),
            rol: signal('CLIENTE'),
            isAuthenticated: signal(true)
          }
        }
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should load profile options', () => {
    const fixture = TestBed.createComponent(PreferencesPage);
    fixture.detectChanges();

    httpMock
      .expectOne('/clientes/cliente-01')
      .flush({ id: 'cliente-01', nombre: 'Cliente', email: 'c@test.local' });
    httpMock.expectOne('/evento/buscar').flush([]);
    httpMock.expectOne('/evento/participantes').flush([]);
    httpMock.expectOne('/lugares').flush([]);

    fixture.detectChanges();
    expect(fixture.componentInstance.loading()).toBe(false);
  });
});
