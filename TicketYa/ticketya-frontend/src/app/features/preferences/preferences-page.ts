import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { AuthStore } from '../../core/auth/auth.store';
import { EventSearchResult, Participant } from '../events/data/event.models';
import { EventService } from '../events/data/event.service';
import { Place } from '../events/data/place.models';
import { PlaceService } from '../events/data/place.service';
import { Customer, PreferenceRequest } from '../customer/data/customer.models';
import { CustomerService } from '../customer/data/customer.service';

interface PreferenceDraft {
  tipo: string;
  ciudad: string;
  idEvento: string;
  idParticipante: string;
  idLugar: string;
}

@Component({
  selector: 'app-preferences-page',
  templateUrl: './preferences-page.html',
  styleUrl: './preferences-page.scss'
})
export class PreferencesPage {
  private readonly authStore = inject(AuthStore);
  private readonly customerService = inject(CustomerService);
  private readonly eventService = inject(EventService);
  private readonly placeService = inject(PlaceService);

  readonly customer = signal<Customer | null>(null);
  readonly events = signal<EventSearchResult[]>([]);
  readonly participants = signal<Participant[]>([]);
  readonly places = signal<Place[]>([]);
  readonly loading = signal(true);
  readonly savingPreference = signal(false);
  readonly savingEmail = signal(false);
  readonly message = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly email = signal('');

  readonly draft = signal<PreferenceDraft>({
    tipo: '',
    ciudad: '',
    idEvento: '',
    idParticipante: '',
    idLugar: ''
  });

  readonly types = computed(() =>
    [
      ...new Set(
        this.events()
          .map((event) => event.tipo)
          .filter(Boolean)
      )
    ].sort()
  );

  readonly cities = computed(() =>
    [
      ...new Set(
        this.places()
          .map((place) => place.ciudad)
          .filter(Boolean)
      )
    ].sort()
  );

  constructor() {
    this.load();
  }

  setTipo(value: string): void {
    this.draft.update((draft) => ({ ...draft, tipo: value }));
  }

  setCiudad(value: string): void {
    this.draft.update((draft) => ({ ...draft, ciudad: value }));
  }

  setEvento(value: string): void {
    this.draft.update((draft) => ({ ...draft, idEvento: value }));
  }

  setParticipante(value: string): void {
    this.draft.update((draft) => ({ ...draft, idParticipante: value }));
  }

  setLugar(value: string): void {
    this.draft.update((draft) => ({ ...draft, idLugar: value }));
  }

  setEmail(value: string): void {
    this.email.set(value);
  }

  savePreference(): void {
    const idCliente = this.authStore.idUsuario();
    if (!idCliente) return;

    const draft = this.draft();
    if (
      !draft.tipo &&
      !draft.ciudad &&
      !draft.idEvento &&
      !draft.idParticipante &&
      !draft.idLugar
    ) {
      this.error.set('Elegí al menos una preferencia');
      return;
    }

    const request: PreferenceRequest = { idCliente };
    if (draft.tipo) request.tipo = draft.tipo;
    if (draft.ciudad) request.ciudad = draft.ciudad;
    if (draft.idEvento) request.idEvento = draft.idEvento;
    if (draft.idParticipante) request.idParticipante = draft.idParticipante;
    if (draft.idLugar) request.idLugar = draft.idLugar;

    this.savingPreference.set(true);
    this.message.set(null);
    this.error.set(null);

    this.customerService.savePreference(request).subscribe({
      next: () => {
        this.savingPreference.set(false);
        this.message.set('Preferencia guardada');
        this.draft.set({ tipo: '', ciudad: '', idEvento: '', idParticipante: '', idLugar: '' });
      },
      error: (error: HttpErrorResponse) => {
        this.savingPreference.set(false);
        this.error.set(this.readError(error, 'No pudimos guardar la preferencia'));
      }
    });
  }

  saveEmail(): void {
    const idCliente = this.authStore.idUsuario();
    const email = this.email().trim();
    if (!idCliente || !email) return;

    this.savingEmail.set(true);
    this.message.set(null);
    this.error.set(null);

    this.customerService.updateEmail(idCliente, email).subscribe({
      next: (customer) => {
        this.savingEmail.set(false);
        this.customer.set(customer);
        this.email.set(customer.email ?? '');
        this.message.set('Email actualizado');
      },
      error: (error: HttpErrorResponse) => {
        this.savingEmail.set(false);
        this.error.set(this.readError(error, 'No pudimos actualizar el email'));
      }
    });
  }

  private load(): void {
    const idCliente = this.authStore.idUsuario();
    if (!idCliente) return;

    forkJoin({
      customer: this.customerService.getCustomer(idCliente),
      events: this.eventService.search(),
      participants: this.eventService.listParticipants(),
      places: this.placeService.list()
    }).subscribe({
      next: ({ customer, events, participants, places }) => {
        this.customer.set(customer);
        this.email.set(customer.email ?? '');
        this.events.set(events);
        this.participants.set(participants);
        this.places.set(places);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('No pudimos cargar tus preferencias');
      }
    });
  }

  private readError(error: HttpErrorResponse, fallback: string): string {
    return typeof error.error === 'string' && error.error.trim() ? error.error : fallback;
  }
}
