import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { PlaceService } from './place.service';

describe('PlaceService', () => {
  let service: PlaceService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PlaceService, provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(PlaceService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should list places through the gateway path', () => {
    service.list().subscribe((places) => {
      expect(places.length).toBe(1);
      expect(places[0].nombre).toBe('Estadio Municipal');
    });

    const request = httpMock.expectOne('/lugares');
    expect(request.request.method).toBe('GET');

    request.flush([
      {
        id: 1,
        nombre: 'Estadio Municipal',
        direccion: 'Av Principal 1200',
        ciudad: 'Tandil',
        latitud: -37.32,
        longitud: -59.13
      }
    ]);
  });
});
