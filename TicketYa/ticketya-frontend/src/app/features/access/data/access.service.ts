import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
export interface AccessTicket {
  id: number;
  idEvento: string;
  idCliente: string;
  qrCode: string;
  usado: boolean;
  estadoDevolucion: string;
}
@Injectable({ providedIn: 'root' })
export class AccessService {
  private readonly http = inject(HttpClient);
  validate(qrCode: string, idEvento: string): Observable<AccessTicket> {
    const params = new HttpParams().set('qrCode', qrCode).set('idEvento', idEvento);
    return this.http.post<AccessTicket>('/ticket/escanearQrcode', null, { params });
  }
}
