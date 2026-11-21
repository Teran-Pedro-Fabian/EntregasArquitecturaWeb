export type SectorType = 'NUMERADO' | 'NO_NUMERADO';
export type PaymentMethod = 'TARJETA' | 'TRANSFERENCIA' | 'BILLETERA_VIRTUAL';

export interface PublicSector {
  idSector: number;
  idLugar: number;
  idEvento: string;
  nombre: string;
  tipo: SectorType;
  precio: number;
  capacidad: number;
}

export interface PublicSeat {
  idAsiento: number;
  idSector: number;
  fila: string;
  columna: number;
  estado: string;
}

export interface GeneralAvailability {
  idSector: number;
  disponibles: number;
}

export interface SeatReservationRequest {
  idAsiento: number;
  idCliente: string;
}

export interface SeatReservationResponse {
  id: number;
  sector: { id: number };
  fila: string;
  columna: number;
  estado: string;
  idClienteReserva: string | null;
  reservaHasta: string;
  idTicketVendido: number | null;
}

export interface GeneralReservationRequest {
  idSector: number;
  idCliente: string;
  cantidad: number;
}

export interface GeneralReservationResponse {
  idReserva: number;
  idSector: number;
  idCliente: string;
  cantidad: number;
  estado: string;
  reservaHasta: string;
}

export interface NumberedPurchaseRequest {
  idCliente: string;
  idEvento: string;
  idSector: number;
  idsAsientos: number[];
  medioPago: PaymentMethod;
  claveIdempotencia?: string;
}

export interface GeneralPurchaseRequest {
  idCliente: string;
  idEvento: string;
  idSector: number;
  idReserva: number;
  cantidad: number;
  medioPago: PaymentMethod;
  claveIdempotencia?: string;
}

export interface TicketResponse {
  id: number;
  idCompra: number | null;
  idEvento: string;
  idCliente: string;
  idSector: number;
  idAsiento: number | null;
  idReservaGeneral: number | null;
  precio: number;
  qrCode: string;
  usado: boolean;
  estadoDevolucion: string;
}

export interface PurchaseResponse {
  idCompra: number;
  estado: string;
  medioPago: string;
  total: number;
  tickets: TicketResponse[];
}

export type ActiveReservation =
  | {
      kind: 'NUMERADO';
      seats: SeatReservationResponse[];
      expiresAt: string;
    }
  | {
      kind: 'NO_NUMERADO';
      reservation: GeneralReservationResponse;
      expiresAt: string;
    };
