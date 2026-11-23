import { computed, Injectable, signal } from '@angular/core';
import { AuthSession, LoginResponse, UserRole } from './auth.models';

const STORAGE_KEY = 'ticketya.auth';

@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly sessionState = signal<AuthSession | null>(this.loadSession());

  readonly session = this.sessionState.asReadonly();
  readonly idUsuario = computed(() => this.sessionState()?.idUsuario ?? null);
  readonly rol = computed(() => this.sessionState()?.rol ?? null);
  readonly token = computed(() => this.sessionState()?.token ?? null);
  readonly isAuthenticated = computed(() => this.token() !== null);

  setSession(response: LoginResponse): void {
    const session: AuthSession = {
      idUsuario: response.idUsuario,
      rol: response.rol,
      token: response.token
    };

    this.sessionState.set(session);
    this.saveSession(session);
  }

  clearSession(): void {
    this.sessionState.set(null);

    if (typeof window !== 'undefined') {
      window.localStorage.removeItem(STORAGE_KEY);
    }
  }

  private loadSession(): AuthSession | null {
    if (typeof window === 'undefined') {
      return null;
    }

    const stored = window.localStorage.getItem(STORAGE_KEY);

    if (!stored) {
      return null;
    }

    try {
      const parsed: unknown = JSON.parse(stored);

      if (!this.isAuthSession(parsed)) {
        window.localStorage.removeItem(STORAGE_KEY);
        return null;
      }

      return parsed;
    } catch {
      window.localStorage.removeItem(STORAGE_KEY);
      return null;
    }
  }

  private saveSession(session: AuthSession): void {
    if (typeof window !== 'undefined') {
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    }
  }

  private isAuthSession(value: unknown): value is AuthSession {
    if (typeof value !== 'object' || value === null) {
      return false;
    }

    const session = value as Record<string, unknown>;

    return (
      typeof session['idUsuario'] === 'string' &&
      typeof session['token'] === 'string' &&
      this.isUserRole(session['rol'])
    );
  }

  private isUserRole(value: unknown): value is UserRole {
    return (
      value === 'CLIENTE' ||
      value === 'ORGANIZADOR' ||
      value === 'ADMINISTRADOR' ||
      value === 'PERSONAL_ACCESO'
    );
  }
}
