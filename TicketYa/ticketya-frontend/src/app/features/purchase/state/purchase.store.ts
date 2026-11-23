import { DestroyRef, computed, inject, Injectable, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import {
  catchError,
  concatMap,
  finalize,
  forkJoin,
  from,
  Observable,
  of,
  switchMap,
  tap,
  throwError,
  toArray
} from 'rxjs';
import { AuthStore } from '../../../core/auth/auth.store';
import { EventDetail } from '../../events/data/event.models';
import { EventService } from '../../events/data/event.service';
import {
  ActiveReservation,
  GeneralReservationResponse,
  PaymentMethod,
  PublicSeat,
  PublicSector,
  PurchaseResponse,
  SeatReservationResponse
} from '../data/purchase.models';
import { PlacePurchaseService } from '../data/place-purchase.service';
import { TicketPurchaseService } from '../data/ticket-purchase.service';
import { VirtualQueueResponse } from '../data/virtual-queue.models';
import { VirtualQueueService } from '../data/virtual-queue.service';

@Injectable()
export class PurchaseStore {
  private readonly authStore = inject(AuthStore);
  private readonly eventService = inject(EventService);
  private readonly placeService = inject(PlacePurchaseService);
  private readonly ticketService = inject(TicketPurchaseService);
  private readonly queueService = inject(VirtualQueueService);
  private readonly destroyRef = inject(DestroyRef);

  readonly event = signal<EventDetail | null>(null);
  readonly sectors = signal<PublicSector[]>([]);
  readonly selectedSectorId = signal<number | null>(null);
  readonly seats = signal<PublicSeat[]>([]);
  readonly selectedSeatIds = signal<number[]>([]);
  readonly availability = signal<number | null>(null);
  readonly quantity = signal(1);
  readonly paymentMethod = signal<PaymentMethod>('TARJETA');
  readonly reservation = signal<ActiveReservation | null>(null);
  readonly purchase = signal<PurchaseResponse | null>(null);
  readonly loading = signal(false);
  readonly actionLoading = signal(false);
  readonly error = signal<string | null>(null);
  readonly remainingSeconds = signal(0);
  readonly queue = signal<VirtualQueueResponse | null>(null);
  readonly queueLoading = signal(false);
  readonly queueError = signal<string | null>(null);

  private timer: ReturnType<typeof setInterval> | null = null;
  private queuePolling: ReturnType<typeof setInterval> | null = null;
  private idempotencyKey: string | null = null;

  readonly selectedSector = computed(
    () => this.sectors().find((sector) => sector.idSector === this.selectedSectorId()) ?? null
  );

  readonly selectedSeats = computed(() => {
    const selected = new Set(this.selectedSeatIds());
    return this.seats().filter((seat) => selected.has(seat.idAsiento));
  });

  readonly total = computed(() => {
    const sector = this.selectedSector();
    if (!sector) return 0;

    const reservation = this.reservation();

    if (reservation) {
      const reservedQuantity =
        reservation.kind === 'NUMERADO'
          ? reservation.seats.length
          : reservation.reservation.cantidad;

      return sector.precio * reservedQuantity;
    }

    if (sector.tipo === 'NUMERADO') {
      return sector.precio * this.selectedSeatIds().length;
    }

    return sector.precio * this.quantity();
  });

  readonly requiresQueue = computed(() => this.event()?.demanda === 'ALTA');
  readonly queueEnabled = computed(
    () => !this.requiresQueue() || this.queue()?.habilitado === true
  );
  readonly soldOut = computed(() => this.event()?.estado === 'AGOTADO');
  readonly reservationExpired = computed(
    () => this.reservation() !== null && this.remainingSeconds() <= 0
  );

  readonly canReserve = computed(() => {
    const sector = this.selectedSector();

    if (!sector || this.reservation() || !this.queueEnabled() || this.soldOut()) {
      return false;
    }

    if (sector.tipo === 'NUMERADO') {
      const count = this.selectedSeatIds().length;
      return count > 0 && count <= 4;
    }

    const available = this.availability() ?? 0;
    return this.quantity() > 0 && this.quantity() <= Math.min(available, 4);
  });

  constructor() {
    this.destroyRef.onDestroy(() => {
      this.clearTimer();
      this.clearQueuePolling();
    });
  }

  load(idEvento: string): void {
    this.loading.set(true);
    this.error.set(null);
    this.resetFlow();

    forkJoin({
      event: this.eventService.getById(idEvento),
      sectors: this.placeService.getSectorsByEvent(idEvento)
    })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: ({ event, sectors }) => {
          this.event.set(event);
          this.sectors.set(sectors);

          if (event.demanda === 'ALTA') {
            this.checkQueueStatus();
          }
        },
        error: (error: HttpErrorResponse) => {
          this.error.set(this.readError(error, 'No pudimos cargar la compra'));
        }
      });
  }

  selectSector(idSector: number): void {
    if (this.reservation()) return;

    const sector = this.sectors().find((item) => item.idSector === idSector);
    if (!sector) return;

    this.selectedSectorId.set(idSector);
    this.selectedSeatIds.set([]);
    this.seats.set([]);
    this.availability.set(null);
    this.quantity.set(1);
    this.error.set(null);

    if (sector.tipo === 'NUMERADO') {
      this.loadSeats(idSector);
      return;
    }

    this.loadAvailability(idSector);
  }

  toggleSeat(idAsiento: number): void {
    if (this.reservation()) return;

    const seat = this.seats().find((item) => item.idAsiento === idAsiento);
    if (!seat || seat.estado !== 'DISPONIBLE') return;

    const selected = this.selectedSeatIds();

    if (selected.includes(idAsiento)) {
      this.selectedSeatIds.set(selected.filter((id) => id !== idAsiento));
      return;
    }

    if (selected.length >= 4) {
      this.error.set('Podes elegir hasta 4 entradas por evento');
      return;
    }

    this.error.set(null);
    this.selectedSeatIds.set([...selected, idAsiento]);
  }

  setQuantity(value: number): void {
    const available = this.availability() ?? 0;
    const next = Math.max(1, Math.min(value, Math.min(available, 4)));
    this.quantity.set(next);
  }

  setPaymentMethod(value: PaymentMethod): void {
    this.paymentMethod.set(value);
  }

  joinQueue(): void {
    const event = this.event();
    const client = this.authStore.idUsuario();

    if (!event || !client || !this.requiresQueue()) return;

    this.queueLoading.set(true);
    this.queueError.set(null);

    this.queueService
      .enter(event.id, client)
      .pipe(finalize(() => this.queueLoading.set(false)))
      .subscribe({
        next: (response) => {
          this.queue.set(response);

          if (response.habilitado) {
            this.clearQueuePolling();
          } else {
            this.startQueuePolling();
          }
        },
        error: (error: HttpErrorResponse) => {
          this.queueError.set(this.readQueueError(error, 'No pudimos ingresar a la fila'));
        }
      });
  }

  refreshQueuePosition(): void {
    const event = this.event();
    const client = this.authStore.idUsuario();

    if (!event || !client || !this.requiresQueue()) return;

    this.queueLoading.set(true);

    this.queueService
      .getPosition(event.id, client)
      .pipe(finalize(() => this.queueLoading.set(false)))
      .subscribe({
        next: (response) => {
          this.queue.set(response);
          this.queueError.set(null);

          if (response.habilitado) {
            this.clearQueuePolling();
          }
        },
        error: (error: HttpErrorResponse) => {
          if (error.status === 400 || error.status === 404) {
            this.queue.set(null);
            this.clearQueuePolling();
            this.queueError.set('Tu lugar en la fila vencio. Ingresa de nuevo');
            return;
          }

          this.queueError.set(this.readQueueError(error, 'No pudimos actualizar tu posicion'));
        }
      });
  }

  reserve(): void {
    const sector = this.selectedSector();
    const client = this.authStore.idUsuario();

    if (!sector || !client || !this.canReserve()) return;

    this.actionLoading.set(true);
    this.error.set(null);

    if (sector.tipo === 'NUMERADO') {
      this.reserveNumbered(sector, client);
      return;
    }

    this.placeService
      .reserveGeneral({
        idSector: sector.idSector,
        idCliente: client,
        cantidad: this.quantity()
      })
      .pipe(finalize(() => this.actionLoading.set(false)))
      .subscribe({
        next: (response) => this.saveGeneralReservation(response),
        error: (error: HttpErrorResponse) => {
          this.error.set(this.readError(error, 'No pudimos reservar los cupos'));
          this.loadAvailability(sector.idSector);
        }
      });
  }

  cancelReservation(): void {
    const reservation = this.reservation();
    const client = this.authStore.idUsuario();
    const sector = this.selectedSector();

    if (!reservation || !client || !sector || this.purchase()) return;

    this.actionLoading.set(true);
    this.error.set(null);

    const request$: Observable<unknown> =
      reservation.kind === 'NUMERADO'
        ? forkJoin(
            reservation.seats.map((seat) =>
              this.placeService
                .cancelSeatReservation(seat.id, client)
                .pipe(catchError(() => of('')))
            )
          )
        : this.placeService.cancelGeneralReservation(
            reservation.reservation.idReserva,
            sector.idSector,
            client
          );

    request$.pipe(finalize(() => this.actionLoading.set(false))).subscribe({
      next: () => {
        this.clearReservation();
        this.refreshSelectedSector();
      },
      error: (error: HttpErrorResponse) => {
        this.error.set(this.readError(error, 'No pudimos cancelar la reserva'));
      }
    });
  }

  buy(): void {
    if (this.actionLoading()) return;
    const reservation = this.reservation();
    const sector = this.selectedSector();
    const event = this.event();
    const client = this.authStore.idUsuario();

    if (
      !reservation ||
      !sector ||
      !event ||
      !client ||
      this.reservationExpired() ||
      !this.queueEnabled()
    )
      return;

    this.actionLoading.set(true);
    this.error.set(null);

    if (!this.idempotencyKey) {
      this.idempotencyKey = this.createIdempotencyKey(event.id, sector.idSector, client);
    }

    const request$: Observable<PurchaseResponse> =
      reservation.kind === 'NUMERADO'
        ? this.ticketService.buyNumbered({
            idCliente: client,
            idEvento: event.id,
            idSector: sector.idSector,
            idsAsientos: reservation.seats.map((seat) => seat.id),
            medioPago: this.paymentMethod(),
            claveIdempotencia: this.idempotencyKey
          })
        : this.ticketService.buyGeneral({
            idCliente: client,
            idEvento: event.id,
            idSector: sector.idSector,
            idReserva: reservation.reservation.idReserva,
            cantidad: reservation.reservation.cantidad,
            medioPago: this.paymentMethod(),
            claveIdempotencia: this.idempotencyKey
          });

    request$.pipe(finalize(() => this.actionLoading.set(false))).subscribe({
      next: (response) => {
        this.purchase.set(response);
        this.clearTimer();

        if (response.estado === 'CONFIRMADA') {
          this.clearQueuePolling();
        }
      },
      error: (error: HttpErrorResponse) => {
        this.error.set(this.readError(error, 'No pudimos completar la compra'));
      }
    });
  }

  retryPendingPurchase(): void {
    const purchase = this.purchase();
    if (!purchase || purchase.estado === 'CONFIRMADA') return;

    this.actionLoading.set(true);
    this.error.set(null);

    this.ticketService
      .retryPurchase(purchase.idCompra)
      .pipe(finalize(() => this.actionLoading.set(false)))
      .subscribe({
        next: (response) => {
          this.purchase.set(response);
          if (response.estado === 'CONFIRMADA') this.clearQueuePolling();
        },
        error: (error: HttpErrorResponse) => {
          this.error.set(this.readError(error, 'No pudimos reintentar la confirmacion'));
        }
      });
  }

  private checkQueueStatus(): void {
    const event = this.event();
    const client = this.authStore.idUsuario();

    if (!event || !client) return;

    this.queueLoading.set(true);

    this.queueService
      .getPosition(event.id, client)
      .pipe(finalize(() => this.queueLoading.set(false)))
      .subscribe({
        next: (response) => {
          this.queue.set(response);
          this.queueError.set(null);

          if (!response.habilitado) {
            this.startQueuePolling();
          }
        },
        error: (error: HttpErrorResponse) => {
          // si no esta en fila no es un error del flujo
          if (error.status === 400 || error.status === 404) {
            this.queue.set(null);
            this.queueError.set(null);
            return;
          }

          this.queueError.set(this.readQueueError(error, 'No pudimos consultar la fila'));
        }
      });
  }

  private startQueuePolling(): void {
    this.clearQueuePolling();

    if (this.queue()?.habilitado) return;

    this.queuePolling = setInterval(() => {
      this.refreshQueuePosition();
    }, 3000);
  }

  private clearQueuePolling(): void {
    if (this.queuePolling) {
      clearInterval(this.queuePolling);
      this.queuePolling = null;
    }
  }

  private loadSeats(idSector: number): void {
    this.actionLoading.set(true);

    this.placeService
      .getSeats(idSector)
      .pipe(finalize(() => this.actionLoading.set(false)))
      .subscribe({
        next: (seats) => this.seats.set(seats),
        error: (error: HttpErrorResponse) => {
          this.error.set(this.readError(error, 'No pudimos cargar los asientos'));
        }
      });
  }

  private loadAvailability(idSector: number): void {
    this.actionLoading.set(true);

    this.placeService
      .getGeneralAvailability(idSector)
      .pipe(finalize(() => this.actionLoading.set(false)))
      .subscribe({
        next: (response) => this.availability.set(response.disponibles),
        error: (error: HttpErrorResponse) => {
          this.error.set(this.readError(error, 'No pudimos consultar la disponibilidad'));
        }
      });
  }

  private reserveNumbered(sector: PublicSector, client: string): void {
    const reserved: SeatReservationResponse[] = [];

    from(this.selectedSeatIds())
      .pipe(
        concatMap((idAsiento) =>
          this.placeService
            .reserveSeat({ idAsiento, idCliente: client })
            .pipe(tap((seat) => reserved.push(seat)))
        ),
        toArray(),
        catchError((error: HttpErrorResponse) =>
          this.cancelPartialNumbered(reserved, client).pipe(
            switchMap(() => throwError(() => error))
          )
        ),
        finalize(() => this.actionLoading.set(false))
      )
      .subscribe({
        next: (seats) => {
          const expiresAt = this.earliestExpiration(seats);
          this.reservation.set({ kind: 'NUMERADO', seats, expiresAt });
          this.idempotencyKey = null;
          this.startTimer(expiresAt);
        },
        error: (error: HttpErrorResponse) => {
          this.error.set(this.readError(error, 'Uno de los asientos ya no esta disponible'));
          this.loadSeats(sector.idSector);
        }
      });
  }

  private cancelPartialNumbered(
    reserved: SeatReservationResponse[],
    client: string
  ): Observable<unknown> {
    if (reserved.length === 0) return of(null);

    return forkJoin(
      reserved.map((seat) =>
        this.placeService.cancelSeatReservation(seat.id, client).pipe(catchError(() => of('')))
      )
    );
  }

  private saveGeneralReservation(response: GeneralReservationResponse): void {
    const expiresAt = response.reservaHasta;

    // desde aca manda la cantidad que realmente quedo reservada
    this.quantity.set(response.cantidad);
    this.reservation.set({ kind: 'NO_NUMERADO', reservation: response, expiresAt });
    this.idempotencyKey = null;
    this.startTimer(expiresAt);
  }

  private refreshSelectedSector(): void {
    const sector = this.selectedSector();
    if (!sector) return;

    if (sector.tipo === 'NUMERADO') {
      this.loadSeats(sector.idSector);
    } else {
      this.loadAvailability(sector.idSector);
    }
  }

  private clearReservation(): void {
    this.reservation.set(null);
    this.purchase.set(null);
    this.idempotencyKey = null;
    this.clearTimer();
  }

  private resetFlow(): void {
    this.event.set(null);
    this.sectors.set([]);
    this.selectedSectorId.set(null);
    this.seats.set([]);
    this.selectedSeatIds.set([]);
    this.availability.set(null);
    this.quantity.set(1);
    this.reservation.set(null);
    this.purchase.set(null);
    this.queue.set(null);
    this.queueError.set(null);
    this.queueLoading.set(false);
    this.idempotencyKey = null;
    this.clearTimer();
    this.clearQueuePolling();
  }

  private earliestExpiration(seats: SeatReservationResponse[]): string {
    return seats
      .map((seat) => seat.reservaHasta)
      .sort((a, b) => new Date(a).getTime() - new Date(b).getTime())[0];
  }

  private startTimer(expiresAt: string): void {
    this.clearTimer();

    const update = () => {
      const remaining = Math.max(0, Math.ceil((new Date(expiresAt).getTime() - Date.now()) / 1000));

      this.remainingSeconds.set(remaining);

      if (remaining === 0) {
        this.clearTimer();
      }
    };

    update();
    this.timer = setInterval(update, 1000);
  }

  private clearTimer(): void {
    if (this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
    this.remainingSeconds.set(0);
  }

  private createIdempotencyKey(eventId: string, sectorId: number, client: string): string {
    const suffix =
      typeof globalThis.crypto?.randomUUID === 'function'
        ? globalThis.crypto.randomUUID()
        : `${Date.now()}-${sectorId}`;

    return `web-${client}-${eventId}-${suffix}`;
  }

  private readQueueError(error: HttpErrorResponse, fallback: string): string {
    if (typeof error.error === 'string' && error.error.trim()) {
      return error.error;
    }

    if (error.status === 401) return 'Necesitas iniciar sesion de nuevo';
    if (error.status === 403) return 'No tenes permisos para esta fila';
    if (error.status === 409) return 'No pudimos asignarte un lugar en la fila';
    if (error.status === 503) return 'La fila virtual no esta disponible en este momento';

    return fallback;
  }

  private readError(error: HttpErrorResponse, fallback: string): string {
    if (typeof error.error === 'string' && error.error.trim()) {
      return error.error;
    }

    if (error.status === 401) return 'Necesitas iniciar sesion de nuevo';
    if (error.status === 403) return 'No tenes permisos para esta operacion';
    if (error.status === 409) return 'La disponibilidad cambio. Revisa tu seleccion';
    if (error.status === 503) return 'Un servicio no esta disponible en este momento';

    return fallback;
  }
}
