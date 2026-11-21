import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { TicketEntry } from './ticket.models';

@Injectable({ providedIn: 'root' })
export class TicketsService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/ticket`;

  getByClient(idCliente: string): Observable<TicketEntry[]> {
    return this.http.get<TicketEntry[]>(
      `${this.baseUrl}/clientes/${encodeURIComponent(idCliente)}`
    );
  }
}
