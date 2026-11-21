import { HttpErrorResponse } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { catchError, finalize, forkJoin, of, switchMap } from 'rxjs';
import { AuthStore } from '../../../core/auth/auth.store';
import { EventDetail } from '../../events/data/event.models';
import { EventService } from '../../events/data/event.service';
import { TicketEntry } from '../data/ticket.models';
import { TicketsService } from '../data/tickets.service';

@Injectable()
export class TicketsStore {
  private readonly authStore = inject(AuthStore);
  private readonly ticketsService = inject(TicketsService);
  private readonly eventService = inject(EventService);

  readonly tickets = signal<TicketEntry[]>([]);
  readonly events = signal<Record<string, EventDetail>>({});
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly empty = computed(() => !this.loading() && this.tickets().length === 0);

  load(): void {
    const idCliente = this.authStore.idUsuario();

    if (!idCliente) {
      this.error.set('No pudimos identificar tu sesion');
      return;
    }

    this.loading.set(true);
    this.error.set(null);
    this.tickets.set([]);
    this.events.set({});

    this.ticketsService
      .getByClient(idCliente)
      .pipe(
        switchMap((tickets) => {
          this.tickets.set(tickets);

          const eventIds = [...new Set(tickets.map((ticket) => ticket.idEvento))];

          if (eventIds.length === 0) {
            return of([] as (EventDetail | null)[]);
          }

          return forkJoin(
            eventIds.map((id) => this.eventService.getById(id).pipe(catchError(() => of(null))))
          );
        }),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: (events) => {
          const byId: Record<string, EventDetail> = {};

          for (const event of events) {
            if (event) {
              byId[event.id] = event;
            }
          }

          this.events.set(byId);
        },
        error: (error: HttpErrorResponse) => {
          if (error.status === 403) {
            this.error.set('No tenes permisos para ver estas entradas');
            return;
          }

          this.error.set('No pudimos cargar tus entradas');
        }
      });
  }

  eventFor(ticket: TicketEntry): EventDetail | null {
    return this.events()[ticket.idEvento] ?? null;
  }
}
