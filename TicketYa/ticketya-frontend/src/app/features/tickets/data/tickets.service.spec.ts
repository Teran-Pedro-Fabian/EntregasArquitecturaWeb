import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TicketsService } from './tickets.service';

describe('TicketsService', () => {
  let service: TicketsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TicketsService, provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(TicketsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should load client tickets', () => {
    service.getByClient('cliente1').subscribe((tickets) => {
      expect(tickets.length).toBe(1);
      expect(tickets[0].id).toBe(10);
    });

    httpMock.expectOne('/ticket/clientes/cliente1').flush([
      {
        id: 10,
        idCompra: 5,
        idEvento: 'evento1',
        idCliente: 'cliente1',
        idSector: 2,
        idAsiento: 4,
        idReservaGeneral: null,
        precio: 12000,
        qrCode: 'TICKETYA-QR',
        usado: false,
        estadoDevolucion: 'SIN_SOLICITUD',
        idAdministradorRevision: null,
        fechaRevision: null
      }
    ]);
  });
});
