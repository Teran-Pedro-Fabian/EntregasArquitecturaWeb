import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormField, FormRoot, form, required } from '@angular/forms/signals';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../../core/auth/auth.service';
import { LoginRequest } from '../../../core/auth/auth.models';

@Component({
  selector: 'app-login',
  imports: [FormField, FormRoot, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly serverError = signal<string | null>(null);

  readonly loginModel = signal<LoginRequest>({
    idUsuario: '',
    password: ''
  });

  readonly loginForm = form(
    this.loginModel,
    (schema) => {
      required(schema.idUsuario, { message: 'Ingresa tu usuario' });
      required(schema.password, { message: 'Ingresa tu contraseña' });
    },
    {
      submission: {
        action: async (field) => {
          this.serverError.set(null);

          try {
            await firstValueFrom(this.authService.login(field().value()));

            // vuelve a la ruta que queria abrir o al inicio
            const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/';
            await this.router.navigateByUrl(returnUrl);

            return;
          } catch (error: unknown) {
            const message = this.getErrorMessage(error);
            this.serverError.set(message);

            return {
              kind: 'login',
              message
            };
          }
        }
      }
    }
  );

  private getErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse && error.status === 401) {
      return 'Usuario o contraseña incorrectos';
    }

    if (error instanceof HttpErrorResponse && error.status === 0) {
      return 'No se pudo conectar con TicketYa';
    }

    return 'No pudimos iniciar sesion. Intenta de nuevo';
  }
}
