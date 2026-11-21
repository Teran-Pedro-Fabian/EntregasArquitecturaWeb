import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  Customer,
  NotificationItem,
  PreferenceRequest,
  PreferenceResponse,
  RecommendationEvent
} from './customer.models';

@Injectable({ providedIn: 'root' })
export class CustomerService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/clientes`;

  getCustomer(id: string): Observable<Customer> {
    return this.http.get<Customer>(`${this.baseUrl}/${encodeURIComponent(id)}`);
  }

  updateEmail(id: string, email: string): Observable<Customer> {
    return this.http.patch<Customer>(`${this.baseUrl}/${encodeURIComponent(id)}/email`, { email });
  }

  savePreference(preference: PreferenceRequest): Observable<PreferenceResponse> {
    return this.http.post<PreferenceResponse>(`${this.baseUrl}/preferencias`, preference);
  }

  getRecommendations(id: string): Observable<RecommendationEvent[]> {
    return this.http.get<RecommendationEvent[]>(
      `${this.baseUrl}/${encodeURIComponent(id)}/recomendaciones`
    );
  }

  getNotifications(id: string): Observable<NotificationItem[]> {
    return this.http.get<NotificationItem[]>(
      `${this.baseUrl}/${encodeURIComponent(id)}/notificaciones`
    );
  }
}
