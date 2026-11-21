import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthStore } from '../../../core/auth/auth.store';
import { TicketsStore } from './tickets.store';

describe('TicketsStore', () => {
  let store: TicketsStore;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        TicketsStore,
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: AuthStore,
          useValue: {
            idUsuario: signal('cliente1').asReadonly()
          }
        }
      ]
    });

    store = TestBed.inject(TicketsStore);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should load tickets and event data', () => {
    store.load();

    httpMock.expectOne('/ticket/clientes/cliente1').flush([
      {
        id: 10,
        idCompra: 5,
        idEvento: 'evento1',
        idCliente: 'cliente1',
        idSector: 2,
        idAsiento: null,
        idReservaGeneral: 8,
        precio: 9000,
        qrCode: 'TICKETYA-QR',
        usado: false,
        estadoDevolucion: 'SIN_SOLICITUD',
        idAdministradorRevision: null,
        fechaRevision: null
      }
    ]);

    httpMock.expectOne('/evento/evento1').flush({
      id: 'evento1',
      nombre: 'Festival',
      descripcion: 'Evento',
      tipo: 'FESTIVAL',
      organizador: 'organizador1',
      fechaHora: '2026-11-30T20:00:00',
      idLugar: 2,
      estado: 'PUBLICADO',
      demanda: 'BAJA'
    });

    expect(store.tickets().length).toBe(1);
    expect(store.eventFor(store.tickets()[0])?.nombre).toBe('Festival');
    expect(store.loading()).toBe(false);
  });

  it('should handle empty tickets', () => {
    store.load();

    httpMock.expectOne('/ticket/clientes/cliente1').flush([]);

    expect(store.empty()).toBe(true);
    expect(store.loading()).toBe(false);
  });
});
