import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { EventDetailStore } from './event-detail.store';

describe('EventDetailStore', () => {
  let store: EventDetailStore;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [EventDetailStore, provideHttpClient(), provideHttpClientTesting()]
    });

    store = TestBed.inject(EventDetailStore);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should load event place and participants', () => {
    store.load('evento-01');

    httpMock.expectOne('/evento/evento-01').flush({
      id: 'evento-01',
      nombre: 'Festival de verano',
      descripcion: 'Festival de musica',
      tipo: 'RECITAL',
      organizador: 'organizador-01',
      fechaHora: '2026-12-15T21:00:00',
      idLugar: 1,
      estado: 'PUBLICADO',
      demanda: 'NORMAL'
    });
    httpMock
      .expectOne('/evento/evento-01/participantes')
      .flush([{ id: 'p-01', nombre: 'Banda invitada' }]);
    httpMock.expectOne('/lugares').flush([
      {
        id: 1,
        nombre: 'Estadio Municipal',
        direccion: 'Av Principal 1200',
        ciudad: 'Tandil',
        latitud: -37.32,
        longitud: -59.13
      }
    ]);

    expect(store.event()?.nombre).toBe('Festival de verano');
    expect(store.place()?.nombre).toBe('Estadio Municipal');
    expect(store.participants()).toHaveLength(1);
    expect(store.loading()).toBe(false);
  });

  it('should show not found error', () => {
    store.load('no-existe');

    httpMock.expectOne('/evento/no-existe').flush('No encontrado', {
      status: 404,
      statusText: 'Not Found'
    });

    httpMock.expectNone('/evento/no-existe/participantes');
    httpMock.expectNone('/lugares');

    expect(store.error()).toBe('No encontramos este evento');
    expect(store.loading()).toBe(false);
  });
});
