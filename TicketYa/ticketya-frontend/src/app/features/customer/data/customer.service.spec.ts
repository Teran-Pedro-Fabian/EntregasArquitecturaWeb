import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CustomerService } from './customer.service';

describe('CustomerService', () => {
  let service: CustomerService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(CustomerService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should save preferences', () => {
    service.savePreference({ idCliente: 'cliente-01', tipo: 'RECITAL' }).subscribe();

    const request = httpMock.expectOne('/clientes/preferencias');
    expect(request.request.method).toBe('POST');
    request.flush({ id: 'pref-01', idCliente: 'cliente-01', tipo: 'RECITAL' });
  });

  it('should get recommendations and notifications', () => {
    service.getRecommendations('cliente-01').subscribe();
    service.getNotifications('cliente-01').subscribe();

    httpMock.expectOne('/clientes/cliente-01/recomendaciones').flush([]);
    httpMock.expectOne('/clientes/cliente-01/notificaciones').flush([]);
  });
});
