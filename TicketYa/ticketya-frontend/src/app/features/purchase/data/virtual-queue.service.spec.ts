import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { VirtualQueueService } from './virtual-queue.service';

describe('VirtualQueueService', () => {
  let service: VirtualQueueService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(VirtualQueueService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should enter virtual queue', () => {
    service.enter('evento-01', 'cliente-01').subscribe();

    const request = httpMock.expectOne('/ticket/cola/evento-01/entrar');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ idCliente: 'cliente-01' });

    request.flush({
      idEvento: 'evento-01',
      idCliente: 'cliente-01',
      posicion: 4,
      habilitado: true
    });
  });

  it('should get queue position', () => {
    service.getPosition('evento-01', 'cliente-01').subscribe();

    const request = httpMock.expectOne(
      (req) =>
        req.url === '/ticket/cola/evento-01/posicion' &&
        req.params.get('idCliente') === 'cliente-01'
    );

    expect(request.request.method).toBe('GET');
    request.flush({
      idEvento: 'evento-01',
      idCliente: 'cliente-01',
      posicion: 12,
      habilitado: false
    });
  });
});
