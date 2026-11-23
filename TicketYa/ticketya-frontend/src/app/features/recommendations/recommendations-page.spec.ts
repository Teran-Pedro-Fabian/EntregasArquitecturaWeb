import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AuthStore } from '../../core/auth/auth.store';
import { RecommendationsPage } from './recommendations-page';

describe('RecommendationsPage', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RecommendationsPage],
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

  it('should load recommendations', () => {
    const fixture = TestBed.createComponent(RecommendationsPage);
    fixture.detectChanges();

    httpMock.expectOne('/clientes/cliente-01/recomendaciones').flush([]);
    httpMock.expectOne('/lugares').flush([]);

    fixture.detectChanges();
    expect(fixture.componentInstance.loading()).toBe(false);
  });
});
