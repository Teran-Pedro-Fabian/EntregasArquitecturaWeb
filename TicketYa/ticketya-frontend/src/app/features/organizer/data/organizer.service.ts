import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EventReport, ManagedEvent, Participant, Place, PublicSector } from './organizer.models';

@Injectable({ providedIn: 'root' })
export class OrganizerService {
  private readonly http = inject(HttpClient);
  listOwn(id: string): Observable<ManagedEvent[]> {
    return this.http.get<ManagedEvent[]>(`/evento/organizador/${encodeURIComponent(id)}`);
  }
  listPlaces(): Observable<Place[]> {
    return this.http.get<Place[]>('/lugares');
  }
  createEvent(event: Omit<ManagedEvent, 'id' | 'estado'>): Observable<ManagedEvent> {
    return this.http.post<ManagedEvent>('/evento', event);
  }
  updateDraft(
    id: string,
    organizer: string,
    event: Omit<ManagedEvent, 'id' | 'estado'>
  ): Observable<ManagedEvent> {
    return this.http.put<ManagedEvent>(`/evento/${id}`, event, {
      params: new HttpParams().set('idOrganizador', organizer)
    });
  }
  publish(id: string, organizer: string): Observable<ManagedEvent> {
    return this.http.put<ManagedEvent>(`/evento/${id}/estado`, null, {
      params: new HttpParams().set('idOrganizador', organizer)
    });
  }
  listParticipants(): Observable<Participant[]> {
    return this.http.get<Participant[]>('/evento/participantes');
  }
  createParticipant(nombre: string): Observable<Participant> {
    return this.http.post<Participant>('/evento/participantes', { nombre });
  }
  addParticipant(eventId: string, participantId: string, organizer: string): Observable<unknown> {
    return this.http.post(`/evento/${eventId}/participantes/${participantId}`, null, {
      params: new HttpParams().set('idOrganizador', organizer)
    });
  }
  sectors(eventId: string): Observable<PublicSector[]> {
    return this.http.get<PublicSector[]>(`/lugares/eventos/${eventId}/sectores`);
  }
  report(eventId: string, organizer: string): Observable<EventReport> {
    return this.http.get<EventReport>(`/ticket/reportes/eventos/${eventId}`, {
      params: new HttpParams().set('idOrganizador', organizer)
    });
  }
}
