import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { AuthStore } from '../../../core/auth/auth.store';
import { AdminEvent, AdminService, RefundTicket } from '../data/admin.service';
@Component({
  selector: 'app-admin-page',
  imports: [DatePipe, CurrencyPipe],
  templateUrl: './admin-page.html',
  styleUrl: './admin-page.scss'
})
export class AdminPage {
  private readonly service = inject(AdminService);
  private readonly auth = inject(AuthStore);
  readonly events = signal<AdminEvent[]>([]);
  readonly refunds = signal<RefundTicket[]>([]);
  readonly error = signal<string | null>(null);
  readonly message = signal<string | null>(null);
  constructor() {
    this.reload();
  }
  reload() {
    this.service
      .events()
      .subscribe({ next: (v) => this.events.set(v), error: (e) => this.fail(e) });
    this.service
      .refunds()
      .subscribe({ next: (v) => this.refunds.set(v), error: (e) => this.fail(e) });
  }
  eventAction(e: AdminEvent, action: 'suspend' | 'reactivate' | 'cancel') {
    const a = this.auth.idUsuario();
    if (!a) return;
    const req =
      action === 'suspend'
        ? this.service.suspend(e.id, a)
        : action === 'reactivate'
          ? this.service.reactivate(e.id, a)
          : this.service.cancel(e.id, a);
    req.subscribe({
      next: () => {
        this.message.set('Evento actualizado');
        this.reload();
      },
      error: (x) => this.fail(x)
    });
  }
  refundAction(t: RefundTicket, action: 'approve' | 'reject' | 'refund' | 'finish') {
    const a = this.auth.idUsuario();
    if (!a) return;
    const req =
      action === 'approve'
        ? this.service.review(t.id, a, 'APROBAR')
        : action === 'reject'
          ? this.service.review(t.id, a, 'RECHAZAR')
          : action === 'refund'
            ? this.service.refund(t.id)
            : this.service.finish(t.id);
    req.subscribe({
      next: () => {
        this.message.set('Devolucion actualizada');
        this.reload();
      },
      error: (x) => this.fail(x)
    });
  }
  private fail(e: HttpErrorResponse) {
    this.error.set(typeof e.error === 'string' ? e.error : 'No se pudo completar la operacion');
  }
}
