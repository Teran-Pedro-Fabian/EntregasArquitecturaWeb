export interface VirtualQueueRequest {
  idCliente: string;
}

export interface VirtualQueueResponse {
  idEvento: string;
  idCliente: string;
  posicion: number;
  habilitado: boolean;
}
