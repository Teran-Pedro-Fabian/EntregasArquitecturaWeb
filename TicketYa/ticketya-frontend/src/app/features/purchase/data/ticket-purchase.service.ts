import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  GeneralPurchaseRequest,
  NumberedPurchaseRequest,
  PurchaseResponse
} from './purchase.models';

@Injectable({ providedIn: 'root' })
export class TicketPurchaseService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/ticket`;

  buyNumbered(request: NumberedPurchaseRequest): Observable<PurchaseResponse> {
    return this.http.post<PurchaseResponse>(`${this.baseUrl}/comprar`, request);
  }

  buyGeneral(request: GeneralPurchaseRequest): Observable<PurchaseResponse> {
    return this.http.post<PurchaseResponse>(`${this.baseUrl}/comprar-general`, request);
  }

  retryPurchase(idCompra: number): Observable<PurchaseResponse> {
    return this.http.post<PurchaseResponse>(`${this.baseUrl}/compras/${idCompra}/reintentar`, {});
  }
}
