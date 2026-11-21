import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { TicketPurchaseService } from './ticket-purchase.service';

describe('TicketPurchaseService', () => {
  let service: TicketPurchaseService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(TicketPurchaseService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should buy general tickets', () => {
    service
      .buyGeneral({
        idCliente: 'cliente-01',
        idEvento: 'evento-01',
        idSector: 3,
        idReserva: 10,
        cantidad: 2,
        medioPago: 'TARJETA',
        claveIdempotencia: 'web-test'
      })
      .subscribe();

    const request = httpMock.expectOne('/ticket/comprar-general');
    expect(request.request.method).toBe('POST');
    expect(request.request.body.idReserva).toBe(10);
    request.flush({
      idCompra: 50,
      estado: 'CONFIRMADA',
      medioPago: 'TARJETA',
      total: 18000,
      tickets: []
    });
  });
});
