import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { VirtualQueueResponse } from './virtual-queue.models';

@Injectable({ providedIn: 'root' })
export class VirtualQueueService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/ticket/cola`;

  enter(idEvento: string, idCliente: string): Observable<VirtualQueueResponse> {
    return this.http.post<VirtualQueueResponse>(`${this.baseUrl}/${idEvento}/entrar`, {
      idCliente
    });
  }

  getPosition(idEvento: string, idCliente: string): Observable<VirtualQueueResponse> {
    const params = new HttpParams().set('idCliente', idCliente);

    return this.http.get<VirtualQueueResponse>(`${this.baseUrl}/${idEvento}/posicion`, { params });
  }
}
