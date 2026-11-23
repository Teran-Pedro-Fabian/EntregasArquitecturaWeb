import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { EventService } from './event.service';

describe('EventService', () => {
  let service: EventService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(EventService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should search events with filters', () => {
    service.search({ ciudad: 'Tandil', ordenarPor: 'fecha' }).subscribe();

    const request = httpMock.expectOne(
      (req) =>
        req.url === '/evento/buscar' &&
        req.params.get('ciudad') === 'Tandil' &&
        req.params.get('ordenarPor') === 'fecha'
    );

    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('should get event detail by id', () => {
    service.getById('evento-01').subscribe((event) => {
      expect(event.id).toBe('evento-01');
    });

    const request = httpMock.expectOne('/evento/evento-01');
    expect(request.request.method).toBe('GET');
    request.flush({
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
  });

  it('should get participants from an event', () => {
    service.getParticipants('evento-01').subscribe((participants) => {
      expect(participants).toHaveLength(1);
    });

    const request = httpMock.expectOne('/evento/evento-01/participantes');
    expect(request.request.method).toBe('GET');
    request.flush([{ id: 'p-01', nombre: 'Banda invitada' }]);
  });

  it('should list participants', () => {
    service.listParticipants().subscribe((participants) => {
      expect(participants).toHaveLength(1);
    });

    const request = httpMock.expectOne('/evento/participantes');
    expect(request.request.method).toBe('GET');
    request.flush([{ id: 'p-01', nombre: 'Banda invitada' }]);
  });
});
