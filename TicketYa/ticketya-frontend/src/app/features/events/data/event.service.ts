import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { EventDetail, EventSearchParams, EventSearchResult, Participant } from './event.models';

@Injectable({
  providedIn: 'root'
})
export class EventService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/evento`;

  search(filters: EventSearchParams = {}): Observable<EventSearchResult[]> {
    let params = new HttpParams();

    // manda solo los filtros que tienen valor
    for (const [key, value] of Object.entries(filters)) {
      if (value !== undefined && value !== null && value !== '') {
        params = params.set(key, String(value));
      }
    }

    return this.http.get<EventSearchResult[]>(`${this.baseUrl}/buscar`, { params });
  }

  getById(id: string): Observable<EventDetail> {
    return this.http.get<EventDetail>(`${this.baseUrl}/${encodeURIComponent(id)}`);
  }

  getParticipants(id: string): Observable<Participant[]> {
    return this.http.get<Participant[]>(`${this.baseUrl}/${encodeURIComponent(id)}/participantes`);
  }

  listParticipants(): Observable<Participant[]> {
    return this.http.get<Participant[]>(`${this.baseUrl}/participantes`);
  }
}
