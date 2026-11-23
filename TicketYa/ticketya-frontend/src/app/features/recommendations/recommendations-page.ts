import { HttpErrorResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthStore } from '../../core/auth/auth.store';
import { Place } from '../events/data/place.models';
import { PlaceService } from '../events/data/place.service';
import { CustomerService } from '../customer/data/customer.service';
import { RecommendationEvent } from '../customer/data/customer.models';

@Component({
  selector: 'app-recommendations-page',
  imports: [RouterLink, DatePipe],
  templateUrl: './recommendations-page.html',
  styleUrl: './recommendations-page.scss'
})
export class RecommendationsPage {
  private readonly authStore = inject(AuthStore);
  private readonly customerService = inject(CustomerService);
  private readonly placeService = inject(PlaceService);

  readonly events = signal<RecommendationEvent[]>([]);
  readonly places = signal<Place[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly placesById = computed(() => new Map(this.places().map((place) => [place.id, place])));

  constructor() {
    this.load();
  }

  reload(): void {
    this.load();
  }

  placeName(event: RecommendationEvent): string {
    const place = this.placesById().get(event.idLugar);
    return place ? `${place.nombre} · ${place.ciudad}` : 'Lugar por confirmar';
  }

  private load(): void {
    const idCliente = this.authStore.idUsuario();
    if (!idCliente) return;

    this.loading.set(true);
    this.error.set(null);

    forkJoin({
      events: this.customerService.getRecommendations(idCliente),
      places: this.placeService.list()
    }).subscribe({
      next: ({ events, places }) => {
        this.events.set(events);
        this.places.set(places);
        this.loading.set(false);
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);
        this.error.set(
          error.status === 503
            ? 'Las recomendaciones no estan disponibles por ahora'
            : 'No pudimos cargar tus recomendaciones'
        );
      }
    });
  }
}
