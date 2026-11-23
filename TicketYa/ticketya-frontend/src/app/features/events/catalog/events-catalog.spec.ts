import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { EventsCatalog } from './events-catalog';

describe('EventsCatalog', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EventsCatalog],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();
  });

  it('should load and render public events', () => {
    const fixture = TestBed.createComponent(EventsCatalog);
    const httpMock = TestBed.inject(HttpTestingController);

    fixture.detectChanges();

    const eventsRequest = httpMock.expectOne(
      (req) => req.url === '/evento/buscar' && req.params.get('ordenarPor') === 'fecha'
    );
    const placesRequest = httpMock.expectOne('/lugares');

    placesRequest.flush([
      {
        id: 1,
        nombre: 'Estadio Municipal',
        direccion: 'Av Principal 1200',
        ciudad: 'Tandil',
        latitud: -37.32,
        longitud: -59.13
      }
    ]);

    eventsRequest.flush([
      {
        id: 'evento-01',
        nombre: 'Festival de verano',
        descripcion: 'Festival de musica',
        tipo: 'RECITAL',
        organizador: 'organizador-01',
        fechaHora: '2026-12-15T21:00:00',
        idLugar: 1,
        estado: 'PUBLICADO',
        demanda: 'BAJA',
        precioDesde: 9000
      }
    ]);

    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Festival de verano');
    expect(compiled.textContent).toContain('Estadio Municipal');
    expect(compiled.textContent).toContain('Ver evento');

    httpMock.verify();
  });
});
