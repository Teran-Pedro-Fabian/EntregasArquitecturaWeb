import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { PlacePurchaseService } from './place-purchase.service';

describe('PlacePurchaseService', () => {
  let service: PlacePurchaseService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(PlacePurchaseService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should get public sectors by event', () => {
    service.getSectorsByEvent('evento-01').subscribe();

    const request = httpMock.expectOne('/lugares/eventos/evento-01/sectores');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('should get seats from a numbered sector', () => {
    service.getSeats(2).subscribe();

    const request = httpMock.expectOne('/lugares/sectores/2/asientos');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('should reserve general tickets', () => {
    service.reserveGeneral({ idSector: 3, idCliente: 'cliente-01', cantidad: 2 }).subscribe();

    const request = httpMock.expectOne('/lugares/sectores/generales/reservar');
    expect(request.request.method).toBe('POST');
    expect(request.request.body.cantidad).toBe(2);
    request.flush({
      idReserva: 10,
      idSector: 3,
      idCliente: 'cliente-01',
      cantidad: 2,
      estado: 'RESERVADA',
      reservaHasta: '2026-12-15T20:10:00Z'
    });
  });
});
