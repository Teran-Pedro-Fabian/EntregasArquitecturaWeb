import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AdminService } from './admin.service';
describe('AdminService', () => {
  it('should list admin events', () => {
    TestBed.configureTestingModule({
      providers: [AdminService, provideHttpClient(), provideHttpClientTesting()]
    });
    const s = TestBed.inject(AdminService),
      h = TestBed.inject(HttpTestingController);
    s.events().subscribe();
    const r = h.expectOne('/evento/admin/todos');
    expect(r.request.method).toBe('GET');
    r.flush([]);
    h.verify();
  });
});
