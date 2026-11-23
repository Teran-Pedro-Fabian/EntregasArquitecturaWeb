import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  GeneralAvailability,
  GeneralReservationRequest,
  GeneralReservationResponse,
  PublicSeat,
  PublicSector,
  SeatReservationRequest,
  SeatReservationResponse
} from './purchase.models';

@Injectable({ providedIn: 'root' })
export class PlacePurchaseService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/lugares`;

  getSectorsByEvent(idEvento: string): Observable<PublicSector[]> {
    return this.http.get<PublicSector[]>(`${this.baseUrl}/eventos/${idEvento}/sectores`);
  }

  getSeats(idSector: number): Observable<PublicSeat[]> {
    return this.http.get<PublicSeat[]>(`${this.baseUrl}/sectores/${idSector}/asientos`);
  }

  getGeneralAvailability(idSector: number): Observable<GeneralAvailability> {
    return this.http.get<GeneralAvailability>(
      `${this.baseUrl}/sectores/${idSector}/disponibilidad`
    );
  }

  reserveSeat(request: SeatReservationRequest): Observable<SeatReservationResponse> {
    return this.http.post<SeatReservationResponse>(`${this.baseUrl}/reservar`, request);
  }

  cancelSeatReservation(idAsiento: number, idCliente: string): Observable<string> {
    const params = new HttpParams().set('idCliente', idCliente);
    return this.http.delete(`${this.baseUrl}/reservas/${idAsiento}`, {
      params,
      responseType: 'text'
    });
  }

  reserveGeneral(request: GeneralReservationRequest): Observable<GeneralReservationResponse> {
    return this.http.post<GeneralReservationResponse>(
      `${this.baseUrl}/sectores/generales/reservar`,
      request
    );
  }

  cancelGeneralReservation(
    idReserva: number,
    idSector: number,
    idCliente: string
  ): Observable<string> {
    const params = new HttpParams().set('idSector', idSector).set('idCliente', idCliente);

    return this.http.delete(`${this.baseUrl}/sectores/generales/reservas/${idReserva}`, {
      params,
      responseType: 'text'
    });
  }
}
