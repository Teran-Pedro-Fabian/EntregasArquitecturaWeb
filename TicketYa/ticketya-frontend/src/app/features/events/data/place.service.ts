import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Place } from './place.models';

@Injectable({
  providedIn: 'root'
})
export class PlaceService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/lugares`;

  // los lugares cambian poco asi que se reutiliza la misma respuesta
  private readonly places$ = this.http
    .get<Place[]>(this.baseUrl)
    .pipe(shareReplay({ bufferSize: 1, refCount: false }));

  list(): Observable<Place[]> {
    return this.places$;
  }
}
