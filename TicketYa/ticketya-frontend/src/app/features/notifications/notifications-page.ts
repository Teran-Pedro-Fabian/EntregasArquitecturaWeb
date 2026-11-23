import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthStore } from '../../core/auth/auth.store';
import { environment } from '../../../environments/environment';
import { NotificationItem } from '../customer/data/customer.models';
import { CustomerService } from '../customer/data/customer.service';

@Component({
  selector: 'app-notifications-page',
  imports: [DatePipe, RouterLink],
  templateUrl: './notifications-page.html',
  styleUrl: './notifications-page.scss'
})
export class NotificationsPage {
  private readonly authStore = inject(AuthStore);
  private readonly customerService = inject(CustomerService);

  readonly notifications = signal<NotificationItem[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly mailpitAvailable = Boolean(environment.mailpitUrl);

  constructor() {
    this.load();
  }

  reload(): void {
    this.load();
  }

  openMailpit(): void {
    if (!environment.mailpitUrl || typeof window === 'undefined') return;

    window.open(
      environment.mailpitUrl,
      'ticketya-mailpit',
      'popup,width=1100,height=800,resizable=yes,scrollbars=yes'
    );
  }

  private load(): void {
    const idCliente = this.authStore.idUsuario();
    if (!idCliente) return;

    this.loading.set(true);
    this.error.set(null);

    this.customerService.getNotifications(idCliente).subscribe({
      next: (notifications) => {
        this.notifications.set(notifications);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('No pudimos cargar las notificaciones');
      }
    });
  }
}
