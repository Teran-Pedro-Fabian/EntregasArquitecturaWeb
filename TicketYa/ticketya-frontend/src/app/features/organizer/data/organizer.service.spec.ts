import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { OrganizerService } from './organizer.service';
describe('OrganizerService', () => {
  it('should list own events', () => {
    TestBed.configureTestingModule({
      providers: [OrganizerService, provideHttpClient(), provideHttpClientTesting()]
    });
    const s = TestBed.inject(OrganizerService),
      h = TestBed.inject(HttpTestingController);
    s.listOwn('org').subscribe();
    const r = h.expectOne('/evento/organizador/org');
    expect(r.request.method).toBe('GET');
    r.flush([]);
    h.verify();
  });
});
