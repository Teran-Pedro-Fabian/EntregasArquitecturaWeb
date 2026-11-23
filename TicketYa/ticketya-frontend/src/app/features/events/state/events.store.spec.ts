import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { EventService } from '../data/event.service';
import { PlaceService } from '../data/place.service';
import { EventsStore } from './events.store';

describe('EventsStore', () => {
  const eventServiceMock = {
    search: vi.fn()
  };

  const placeServiceMock = {
    list: vi.fn()
  };

  beforeEach(() => {
    eventServiceMock.search.mockReset();
    placeServiceMock.list.mockReset();

    TestBed.configureTestingModule({
      providers: [
        EventsStore,
        { provide: EventService, useValue: eventServiceMock },
        { provide: PlaceService, useValue: placeServiceMock }
      ]
    });
  });

  it('should keep events and resolve their place', () => {
    eventServiceMock.search.mockReturnValue(
      of([
        {
          id: 'evento-01',
          nombre: 'Festival',
          descripcion: 'Festival de musica',
          tipo: 'RECITAL',
          organizador: 'organizador-01',
          fechaHora: '2026-12-15T21:00:00',
          idLugar: 1,
          estado: 'PUBLICADO',
          demanda: 'BAJA',
          precioDesde: 9000
        }
      ])
    );

    placeServiceMock.list.mockReturnValue(
      of([
        {
          id: 1,
          nombre: 'Estadio Municipal',
          direccion: 'Av Principal 1200',
          ciudad: 'Tandil',
          latitud: -37.32,
          longitud: -59.13
        }
      ])
    );

    const store = TestBed.inject(EventsStore);
    store.search({ ordenarPor: 'fecha' });

    expect(store.events().length).toBe(1);
    expect(store.placeById(1)?.nombre).toBe('Estadio Municipal');
    expect(store.loading()).toBe(false);
    expect(store.error()).toBeNull();
  });

  it('should expose a readable error when the catalog fails', () => {
    eventServiceMock.search.mockReturnValue(throwError(() => new Error('backend down')));
    placeServiceMock.list.mockReturnValue(of([]));

    const store = TestBed.inject(EventsStore);
    store.search();

    expect(store.events()).toEqual([]);
    expect(store.loading()).toBe(false);
    expect(store.error()).toBe('No pudimos cargar los eventos. Intenta de nuevo');
  });
});
