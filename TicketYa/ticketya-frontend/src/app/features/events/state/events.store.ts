import { computed, inject, Injectable, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { EventSearchParams, EventSearchResult } from '../data/event.models';
import { EventService } from '../data/event.service';
import { Place } from '../data/place.models';
import { PlaceService } from '../data/place.service';

@Injectable()
export class EventsStore {
  private readonly eventService = inject(EventService);
  private readonly placeService = inject(PlaceService);

  private readonly _events = signal<EventSearchResult[]>([]);
  private readonly _places = signal<Place[]>([]);
  private readonly _loading = signal(false);
  private readonly _error = signal<string | null>(null);

  readonly events = this._events.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly error = this._error.asReadonly();
  readonly hasEvents = computed(() => this._events().length > 0);

  search(filters: EventSearchParams = {}): void {
    this._loading.set(true);
    this._error.set(null);

    forkJoin({
      events: this.eventService.search(filters),
      places: this.placeService.list()
    }).subscribe({
      next: ({ events, places }) => {
        this._events.set(events);
        this._places.set(places);
        this._loading.set(false);
      },
      error: () => {
        this._events.set([]);
        this._loading.set(false);
        this._error.set('No pudimos cargar los eventos. Intenta de nuevo');
      }
    });
  }

  placeById(idLugar: number): Place | null {
    return this._places().find((place) => place.id === idLugar) ?? null;
  }
}
