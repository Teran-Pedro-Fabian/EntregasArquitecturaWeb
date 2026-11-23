import { Component, inject, OnInit, signal } from '@angular/core';
import { FormField, FormRoot, form } from '@angular/forms/signals';
import { RouterLink } from '@angular/router';
import { EventOrder, EventSearchParams } from '../data/event.models';
import { EventsStore } from '../state/events.store';

interface CatalogFilters {
  texto: string;
  tipo: string;
  ciudad: string;
  fechaDesde: string;
  fechaHasta: string;
  ordenarPor: EventOrder;
}

const DEFAULT_FILTERS: CatalogFilters = {
  texto: '',
  tipo: '',
  ciudad: '',
  fechaDesde: '',
  fechaHasta: '',
  ordenarPor: 'fecha'
};

@Component({
  selector: 'app-events-catalog',
  imports: [FormField, FormRoot, RouterLink],
  providers: [EventsStore],
  templateUrl: './events-catalog.html',
  styleUrl: './events-catalog.scss'
})
export class EventsCatalog implements OnInit {
  protected readonly store = inject(EventsStore);

  readonly filterModel = signal<CatalogFilters>({ ...DEFAULT_FILTERS });
  readonly filterForm = form(this.filterModel);

  private readonly priceFormatter = new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
    maximumFractionDigits: 0
  });

  ngOnInit(): void {
    this.search();
  }

  search(): void {
    const value = this.filterModel();
    const filters: EventSearchParams = {
      texto: value.texto.trim(),
      tipo: value.tipo.trim(),
      ciudad: value.ciudad.trim(),
      fechaDesde: value.fechaDesde,
      fechaHasta: value.fechaHasta,
      ordenarPor: value.ordenarPor
    };

    this.store.search(filters);
  }

  clearFilters(): void {
    this.filterModel.set({ ...DEFAULT_FILTERS });
    this.search();
  }

  formatDate(value: string): string {
    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
      return value;
    }

    return new Intl.DateTimeFormat('es-AR', {
      dateStyle: 'medium',
      timeStyle: 'short'
    }).format(date);
  }

  formatPrice(value: number | null): string {
    return value === null ? 'Precio no disponible' : `Desde ${this.priceFormatter.format(value)}`;
  }
}
