export interface Customer {
  id: string;
  nombre: string;
  email?: string | null;
}

export interface PreferenceRequest {
  idCliente: string;
  idEvento?: string;
  idParticipante?: string;
  idLugar?: string;
  ciudad?: string;
  tipo?: string;
}

export interface PreferenceResponse extends PreferenceRequest {
  id: string;
}

export interface NotificationItem {
  id: number;
  idEvento: string;
  idCliente: string;
  mensaje: string;
  fechaCreacion: string;
}

export interface RecommendationEvent {
  id: string;
  nombre: string;
  descripcion: string;
  tipo: string;
  organizador: string;
  fechaHora: string;
  idLugar: number;
  estado: string;
  demanda: string;
  precioDesde: number | null;
}
