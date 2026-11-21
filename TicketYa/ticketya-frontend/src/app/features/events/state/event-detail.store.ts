import { HttpErrorResponse } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { finalize, forkJoin, of, switchMap } from 'rxjs';
import { EventDetail, Participant } from '../data/event.models';
import { EventService } from '../data/event.service';
import { Place } from '../data/place.models';
import { PlaceService } from '../data/place.service';

@Injectable()
export class EventDetailStore {
  private readonly eventService = inject(EventService);
  private readonly placeService = inject(PlaceService);

  readonly event = signal<EventDetail | null>(null);
  readonly place = signal<Place | null>(null);
  readonly participants = signal<Participant[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly found = computed(() => this.event() !== null);
  readonly soldOut = computed(() => this.event()?.estado === 'AGOTADO');

  load(id: string): void {
    this.loading.set(true);
    this.error.set(null);
    this.event.set(null);
    this.place.set(null);
    this.participants.set([]);

    this.eventService
      .getById(id)
      .pipe(
        switchMap((event) =>
          forkJoin({
            event: of(event),
            participants: this.eventService.getParticipants(id),
            places: this.placeService.list()
          })
        ),
        finalize(() => this.loading.set(false))
      )
      .subscribe({
        next: ({ event, participants, places }) => {
          this.event.set(event);
          this.participants.set(participants);
          this.place.set(places.find((place) => place.id === event.idLugar) ?? null);
        },
        error: (error: HttpErrorResponse) => {
          if (error.status === 404) {
            this.error.set('No encontramos este evento');
            return;
          }

          this.error.set('No pudimos cargar el evento');
        }
      });
  }
}
