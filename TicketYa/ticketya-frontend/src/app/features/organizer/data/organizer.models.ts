export type ManagedEventState =
  'BORRADOR' | 'PUBLICADO' | 'SUSPENDIDO' | 'AGOTADO' | 'CANCELADO' | 'FINALIZADO';
export interface ManagedEvent {
  id: string;
  nombre: string;
  descripcion: string;
  tipo: string;
  organizador: string;
  fechaHora: string;
  idLugar: number;
  estado: ManagedEventState;
  demanda: string;
}
export interface Place {
  id: number;
  nombre: string;
  direccion: string;
  ciudad: string | null;
  latitud: number;
  longitud: number;
}
export interface Participant {
  id: string;
  nombre: string;
}
export interface PublicSector {
  idSector: number;
  idLugar: number;
  idEvento: string;
  nombre: string;
  tipo: string;
  precio: number;
  capacidad: number;
}
export interface EventReportSector {
  idSector: number;
  nombre: string;
  tipo: string;
  capacidad: number;
  entradasVendidas: number;
  entradasDevueltas: number;
  entradasActivas: number;
  ocupacionPorcentaje: number;
  recaudacionBruta: number;
  reintegrosRealizados: number;
  recaudacionNeta: number;
}
export interface EventReport {
  idEvento: string;
  nombreEvento: string;
  organizador: string;
  sectores: EventReportSector[];
  capacidadTotal: number;
  entradasVendidas: number;
  entradasDevueltas: number;
  entradasActivas: number;
  ocupacionPorcentaje: number;
  recaudacionBruta: number;
  reintegrosRealizados: number;
  recaudacionNeta: number;
}
