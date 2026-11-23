export type UserRole = 'CLIENTE' | 'ORGANIZADOR' | 'ADMINISTRADOR' | 'PERSONAL_ACCESO';

export interface LoginRequest {
  idUsuario: string;
  password: string;
}

export interface LoginResponse {
  idUsuario: string;
  rol: UserRole;
  token: string;
}

export interface AuthSession {
  idUsuario: string;
  rol: UserRole;
  token: string;
}
