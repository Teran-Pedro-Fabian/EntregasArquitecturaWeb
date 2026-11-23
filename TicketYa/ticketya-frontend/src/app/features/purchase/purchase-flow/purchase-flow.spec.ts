import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { PurchaseFlow } from './purchase-flow';

describe('PurchaseFlow', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PurchaseFlow],
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

  it('should load event and sectors', () => {
    const fixture = TestBed.createComponent(PurchaseFlow);
    const httpMock = TestBed.inject(HttpTestingController);

    fixture.detectChanges();

    httpMock.expectOne('/evento/evento-01').flush({
      id: 'evento-01',
      nombre: 'Festival de verano',
      descripcion: 'Festival de musica',
      tipo: 'FESTIVAL',
      organizador: 'organizador-01',
      fechaHora: '2026-12-15T21:00:00',
      idLugar: 1,
      estado: 'PUBLICADO',
      demanda: 'BAJA'
    });

    httpMock.expectOne('/lugares/eventos/evento-01/sectores').flush([
      {
        idSector: 3,
        idLugar: 1,
        idEvento: 'evento-01',
        nombre: 'Campo',
        tipo: 'NO_NUMERADO',
        precio: 9000,
        capacidad: 100
      }
    ]);

    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Festival de verano');
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';

    expect(text).toContain('Campo');
    expect(text).not.toContain('NO_NUMERADO');

    httpMock.verify();
  });

  it('should show virtual queue for high demand event', () => {
    const fixture = TestBed.createComponent(PurchaseFlow);
    const httpMock = TestBed.inject(HttpTestingController);

    fixture.detectChanges();

    httpMock.expectOne('/evento/evento-01').flush({
      id: 'evento-01',
      nombre: 'Recital masivo',
      descripcion: 'Alta demanda',
      tipo: 'RECITAL',
      organizador: 'organizador-01',
      fechaHora: '2026-12-20T21:00:00',
      idLugar: 1,
      estado: 'PUBLICADO',
      demanda: 'ALTA'
    });

    httpMock.expectOne('/lugares/eventos/evento-01/sectores').flush([]);

    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Este evento tiene alta demanda');
    expect(text).toContain('Ingresar a la fila');

    httpMock.verify();
  });
});
