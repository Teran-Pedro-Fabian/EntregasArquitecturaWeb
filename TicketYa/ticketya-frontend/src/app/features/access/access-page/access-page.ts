import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AccessService, AccessTicket } from '../data/access.service';
@Component({
  selector: 'app-access-page',
  imports: [FormsModule],
  templateUrl: './access-page.html',
  styleUrl: './access-page.scss'
})
export class AccessPage {
  private readonly service = inject(AccessService);
  idEvento = '';
  qrCode = '';
  readonly result = signal<AccessTicket | null>(null);
  readonly message = signal('');
  readonly status = signal<'idle' | 'ok' | 'error'>('idle');
  validate() {
    this.result.set(null);
    this.message.set('');
    this.service.validate(this.qrCode.trim(), this.idEvento.trim()).subscribe({
      next: (v) => {
        this.result.set(v);
        this.status.set('ok');
        this.message.set('Entrada valida · ingreso registrado');
      },
      error: (e: HttpErrorResponse) => {
        this.status.set('error');
        if (e.status === 404) this.message.set('Entrada inexistente');
        else if (e.status === 409)
          this.message.set(
            typeof e.error === 'string' ? e.error : 'Entrada usada o de otro evento'
          );
        else this.message.set('No se pudo validar la entrada');
      }
    });
  }
}
