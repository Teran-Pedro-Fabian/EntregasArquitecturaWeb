import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AccessService } from './access.service';
describe('AccessService', () => {
  it('should validate qr', () => {
    TestBed.configureTestingModule({
      providers: [AccessService, provideHttpClient(), provideHttpClientTesting()]
    });
    const s = TestBed.inject(AccessService),
      h = TestBed.inject(HttpTestingController);
    s.validate('QR', 'evt').subscribe();
    const r = h.expectOne((x) => x.url === '/ticket/escanearQrcode');
    expect(r.request.method).toBe('POST');
    expect(r.request.params.get('qrCode')).toBe('QR');
    r.flush({
      id: 1,
      idEvento: 'evt',
      idCliente: 'c',
      qrCode: 'QR',
      usado: true,
      estadoDevolucion: 'SIN_SOLICITUD'
    });
    h.verify();
  });
});
