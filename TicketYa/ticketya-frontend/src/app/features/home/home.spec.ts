import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Home } from './home';

describe('Home', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Home],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should load the first three events', async () => {
    const fixture = TestBed.createComponent(Home);
    fixture.detectChanges();

    const request = httpMock.expectOne(
      (req) => req.url === '/evento/buscar' && req.params.get('ordenarPor') === 'fecha'
    );

    request.flush([
      {
        id: 'evento-1',
        nombre: 'Evento uno',
        descripcion: 'Descripcion',
        tipo: 'RECITAL',
        organizador: 'org-1',
        fechaHora: '2026-12-01T20:00:00',
        idLugar: 1,
        estado: 'PUBLICADO',
        demanda: 'BAJA',
        precioDesde: 10000
      },
      {
        id: 'evento-2',
        nombre: 'Evento dos',
        descripcion: 'Descripcion',
        tipo: 'FESTIVAL',
        organizador: 'org-1',
        fechaHora: '2026-12-02T20:00:00',
        idLugar: 1,
        estado: 'PUBLICADO',
        demanda: 'BAJA',
        precioDesde: 12000
      },
      {
        id: 'evento-3',
        nombre: 'Evento tres',
        descripcion: 'Descripcion',
        tipo: 'TEATRO',
        organizador: 'org-1',
        fechaHora: '2026-12-03T20:00:00',
        idLugar: 1,
        estado: 'PUBLICADO',
        demanda: 'BAJA',
        precioDesde: 8000
      },
      {
        id: 'evento-4',
        nombre: 'Evento cuatro',
        descripcion: 'Descripcion',
        tipo: 'RECITAL',
        organizador: 'org-1',
        fechaHora: '2026-12-04T20:00:00',
        idLugar: 1,
        estado: 'PUBLICADO',
        demanda: 'BAJA',
        precioDesde: 9000
      }
    ]);

    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.componentInstance.events()).toHaveLength(3);
    expect(fixture.nativeElement.textContent).toContain('Evento uno');
    expect(fixture.nativeElement.textContent).not.toContain('Evento cuatro');
  });
});
