export type EventState =
  'BORRADOR' | 'PUBLICADO' | 'SUSPENDIDO' | 'AGOTADO' | 'CANCELADO' | 'FINALIZADO';

export type EventOrder = 'fecha' | 'precio' | 'relevancia';

export interface EventSearchParams {
  tipo?: string;
  organizador?: string;
  participante?: string;
  ciudad?: string;
  fechaDesde?: string;
  fechaHasta?: string;
  texto?: string;
  q?: string;
  lat?: number;
  lon?: number;
  ordenarPor?: EventOrder;
}

export interface EventDetail {
  id: string;
  nombre: string;
  descripcion: string;
  tipo: string;
  organizador: string;
  fechaHora: string;
  idLugar: number;
  estado: EventState;
  demanda: string;
}

export interface EventSearchResult extends EventDetail {
  precioDesde: number | null;
}

export interface Participant {
  id: string;
  nombre: string;
}
