import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
export interface AdminEvent {
  id: string;
  nombre: string;
  descripcion: string;
  tipo: string;
  organizador: string;
  fechaHora: string;
  idLugar: number;
  estado: string;
  demanda: string;
}
export interface RefundTicket {
  id: number;
  idEvento: string;
  idCliente: string;
  precio: number;
  estadoDevolucion: string;
  idAdministradorRevision: string | null;
  fechaRevision: string | null;
}
@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);
  events(): Observable<AdminEvent[]> {
    return this.http.get<AdminEvent[]>('/evento/admin/todos');
  }
  refunds(estado?: string): Observable<RefundTicket[]> {
    return this.http.get<RefundTicket[]>('/ticket/devoluciones', {
      params: estado ? new HttpParams().set('estado', estado) : undefined
    });
  }
  suspend(id: string, admin: string) {
    return this.http.patch<AdminEvent>(`/evento/${id}/suspender`, null, {
      params: new HttpParams().set('idAdministrador', admin)
    });
  }
  reactivate(id: string, admin: string) {
    return this.http.patch<AdminEvent>(`/evento/${id}/reactivar`, null, {
      params: new HttpParams().set('idAdministrador', admin)
    });
  }
  cancel(id: string, admin: string) {
    return this.http.patch<AdminEvent>(`/evento/${id}/cancelar`, null, {
      params: new HttpParams().set('idAdministrador', admin)
    });
  }
  review(id: number, admin: string, decision: 'APROBAR' | 'RECHAZAR') {
    return this.http.patch<RefundTicket>(`/ticket/${id}/revisar-devolucion`, null, {
      params: new HttpParams().set('idAdministrador', admin).set('decision', decision)
    });
  }
  refund(id: number) {
    return this.http.patch<RefundTicket>(`/ticket/${id}/procesar-reintegro`, null);
  }
  finish(id: number) {
    return this.http.patch<RefundTicket>(`/ticket/${id}/finalizar-devolucion`, null);
  }
}
