import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { AuthStore } from '../../../core/auth/auth.store';
import { EventService } from '../../events/data/event.service';
import { PlacePurchaseService } from '../data/place-purchase.service';
import { TicketPurchaseService } from '../data/ticket-purchase.service';
import { VirtualQueueService } from '../data/virtual-queue.service';
import { PurchaseStore } from './purchase.store';

describe('PurchaseStore', () => {
  const authStoreMock = {
    idUsuario: vi.fn(() => 'cliente-01')
  };

  const eventServiceMock = {
    getById: vi.fn()
  };

  const placeServiceMock = {
    getSectorsByEvent: vi.fn(),
    getSeats: vi.fn(),
    getGeneralAvailability: vi.fn(),
    reserveSeat: vi.fn(),
    cancelSeatReservation: vi.fn(),
    reserveGeneral: vi.fn(),
    cancelGeneralReservation: vi.fn()
  };

  const ticketServiceMock = {
    buyNumbered: vi.fn(),
    buyGeneral: vi.fn(),
    retryPurchase: vi.fn()
  };

  const queueServiceMock = {
    enter: vi.fn(),
    getPosition: vi.fn()
  };

  beforeEach(() => {
    vi.useFakeTimers();
    vi.clearAllMocks();

    eventServiceMock.getById.mockReturnValue(
      of({
        id: 'evento-01',
        nombre: 'Festival',
        descripcion: 'Evento de prueba',
        tipo: 'FESTIVAL',
        organizador: 'organizador-01',
        fechaHora: '2026-12-15T21:00:00',
        idLugar: 1,
        estado: 'PUBLICADO',
        demanda: 'BAJA'
      })
    );

    placeServiceMock.getSectorsByEvent.mockReturnValue(
      of([
        {
          idSector: 3,
          idLugar: 1,
          idEvento: 'evento-01',
          nombre: 'Campo',
          tipo: 'NO_NUMERADO',
          precio: 9000,
          capacidad: 100
        }
      ])
    );

    placeServiceMock.getGeneralAvailability.mockReturnValue(
      of({
        idSector: 3,
        disponibles: 10
      })
    );

    TestBed.configureTestingModule({
      providers: [
        PurchaseStore,
        { provide: AuthStore, useValue: authStoreMock },
        { provide: EventService, useValue: eventServiceMock },
        { provide: PlacePurchaseService, useValue: placeServiceMock },
        { provide: TicketPurchaseService, useValue: ticketServiceMock },
        { provide: VirtualQueueService, useValue: queueServiceMock }
      ]
    });
  });

  afterEach(() => {
    TestBed.resetTestingModule();
    vi.useRealTimers();
  });

  it('should load sectors and availability', () => {
    const store = TestBed.inject(PurchaseStore);

    store.load('evento-01');
    store.selectSector(3);

    expect(store.sectors()).toHaveLength(1);
    expect(store.selectedSector()?.nombre).toBe('Campo');
    expect(store.availability()).toBe(10);
    expect(store.canReserve()).toBe(true);
  });

  it('should reserve and buy general tickets', () => {
    placeServiceMock.reserveGeneral.mockReturnValue(
      of({
        idReserva: 10,
        idSector: 3,
        idCliente: 'cliente-01',
        cantidad: 4,
        estado: 'RESERVADA',
        reservaHasta: '2099-12-15T20:10:00Z'
      })
    );

    ticketServiceMock.buyGeneral.mockReturnValue(
      of({
        idCompra: 50,
        estado: 'CONFIRMADA',
        medioPago: 'TARJETA',
        total: 36000,
        tickets: []
      })
    );

    const store = TestBed.inject(PurchaseStore);
    store.load('evento-01');
    store.selectSector(3);
    store.setQuantity(4);
    store.reserve();

    expect(store.reservation()?.kind).toBe('NO_NUMERADO');
    expect(store.quantity()).toBe(4);
    expect(store.total()).toBe(36000);

    // aunque cambie el input, la reserva queda como fuente de verdad
    store.setQuantity(1);
    expect(store.total()).toBe(36000);

    store.buy();

    expect(ticketServiceMock.buyGeneral).toHaveBeenCalledWith(
      expect.objectContaining({
        cantidad: 4
      })
    );
    expect(store.purchase()?.estado).toBe('CONFIRMADA');
  });

  it('should require virtual queue for high demand events', () => {
    eventServiceMock.getById.mockReturnValue(
      of({
        id: 'evento-alta',
        nombre: 'Recital masivo',
        descripcion: 'Evento de prueba',
        tipo: 'RECITAL',
        organizador: 'organizador-01',
        fechaHora: '2026-12-20T21:00:00',
        idLugar: 1,
        estado: 'PUBLICADO',
        demanda: 'ALTA'
      })
    );

    queueServiceMock.getPosition.mockReturnValue(throwError(() => ({ status: 400 })));
    queueServiceMock.enter.mockReturnValue(
      of({
        idEvento: 'evento-alta',
        idCliente: 'cliente-01',
        posicion: 15,
        habilitado: false
      })
    );

    const store = TestBed.inject(PurchaseStore);
    store.load('evento-alta');
    store.selectSector(3);

    expect(store.requiresQueue()).toBe(true);
    expect(store.canReserve()).toBe(false);

    store.joinQueue();

    expect(store.queue()?.posicion).toBe(15);
    expect(store.queueEnabled()).toBe(false);

    queueServiceMock.getPosition.mockReturnValue(
      of({
        idEvento: 'evento-alta',
        idCliente: 'cliente-01',
        posicion: 1,
        habilitado: true
      })
    );

    store.refreshQueuePosition();

    expect(store.queueEnabled()).toBe(true);
    expect(store.canReserve()).toBe(true);
  });
});
