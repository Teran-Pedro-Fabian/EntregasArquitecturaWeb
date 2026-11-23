import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthStore } from '../../core/auth/auth.store';
import { NotificationsPage } from './notifications-page';

describe('NotificationsPage', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NotificationsPage],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: AuthStore, useValue: { idUsuario: signal('cliente-01') } }
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should load notifications', () => {
    const fixture = TestBed.createComponent(NotificationsPage);
    fixture.detectChanges();

    httpMock.expectOne('/clientes/cliente-01/notificaciones').flush([]);

    fixture.detectChanges();
    expect(fixture.componentInstance.loading()).toBe(false);
  });
});
