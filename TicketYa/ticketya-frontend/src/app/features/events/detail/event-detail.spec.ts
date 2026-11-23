import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { EventDetailPage } from './event-detail';

describe('EventDetailPage', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EventDetailPage],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ id: 'evento-01' })
            }
          }
        },
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();
  });

  it('should render event detail', () => {
    const fixture = TestBed.createComponent(EventDetailPage);
    const httpMock = TestBed.inject(HttpTestingController);

    fixture.detectChanges();

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

    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Festival de verano');
    expect(compiled.textContent).toContain('Estadio Municipal');
    expect(compiled.textContent).toContain('Banda invitada');

    httpMock.verify();
  });
});
