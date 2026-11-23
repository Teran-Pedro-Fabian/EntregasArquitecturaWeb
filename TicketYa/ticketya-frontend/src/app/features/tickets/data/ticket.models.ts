export interface TicketEntry {
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
  idAdministradorRevision: string | null;
  fechaRevision: string | null;
}
