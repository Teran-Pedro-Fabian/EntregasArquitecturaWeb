import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthStore } from '../../../core/auth/auth.store';
import {
  EventReport,
  ManagedEvent,
  Participant,
  Place,
  PublicSector
} from '../data/organizer.models';
import { OrganizerService } from '../data/organizer.service';

@Component({
  selector: 'app-organizer-dashboard',
  imports: [FormsModule, DatePipe, CurrencyPipe],
  templateUrl: './organizer-dashboard.html',
  styleUrl: './organizer-dashboard.scss'
})
export class OrganizerDashboard {
  private readonly service = inject(OrganizerService);
  private readonly auth = inject(AuthStore);
  readonly events = signal<ManagedEvent[]>([]);
  readonly places = signal<Place[]>([]);
  readonly participants = signal<Participant[]>([]);
  readonly sectors = signal<PublicSector[]>([]);
  readonly report = signal<EventReport | null>(null);
  readonly selected = signal<ManagedEvent | null>(null);
  readonly error = signal<string | null>(null);
  readonly message = signal<string | null>(null);
  nombre = '';
  descripcion = '';
  tipo = 'RECITAL';
  fechaHora = '';
  idLugar: number | null = null;
  demanda = 'BAJA';
  participantName = '';
  selectedParticipant = '';
  constructor() {
    this.reload();
    this.service.listPlaces().subscribe((v) => this.places.set(v));
    this.service.listParticipants().subscribe((v) => this.participants.set(v));
  }
  reload() {
    const id = this.auth.idUsuario();
    if (!id) return;
    this.service.listOwn(id).subscribe({
      next: (v) => this.events.set(v),
      error: () => this.error.set('No pudimos cargar tus eventos')
    });
  }
  select(event: ManagedEvent) {
    this.selected.set(event);
    this.report.set(null);
    this.service.sectors(event.id).subscribe((v) => this.sectors.set(v));
  }
  create() {
    const org = this.auth.idUsuario();
    if (!org || !this.idLugar) return;
    this.clearStatus();
    this.service
      .createEvent({
        nombre: this.nombre,
        descripcion: this.descripcion,
        tipo: this.tipo,
        organizador: org,
        fechaHora: this.fechaHora,
        idLugar: this.idLugar,
        demanda: this.demanda
      })
      .subscribe({
        next: (e) => {
          this.message.set('Evento creado');
          this.resetForm();
          this.reload();
          this.select(e);
        },
        error: (e) => this.fail(e)
      });
  }
  loadForEdit(e: ManagedEvent) {
    this.select(e);
    this.nombre = e.nombre;
    this.descripcion = e.descripcion;
    this.tipo = e.tipo;
    this.fechaHora = e.fechaHora;
    this.idLugar = e.idLugar;
    this.demanda = e.demanda;
  }
  update() {
    const e = this.selected(),
      org = this.auth.idUsuario();
    if (!e || !org || !this.idLugar) return;
    this.clearStatus();
    this.service
      .updateDraft(e.id, org, {
        nombre: this.nombre,
        descripcion: this.descripcion,
        tipo: this.tipo,
        organizador: org,
        fechaHora: this.fechaHora,
        idLugar: this.idLugar,
        demanda: this.demanda
      })
      .subscribe({
        next: (v) => {
          this.message.set('Evento actualizado');
          this.reload();
          this.select(v);
        },
        error: (x) => this.fail(x)
      });
  }
  publish() {
    const e = this.selected(),
      org = this.auth.idUsuario();
    if (!e || !org) return;
    this.service.publish(e.id, org).subscribe({
      next: (v) => {
        this.message.set('Evento publicado');
        this.reload();
        this.select(v);
      },
      error: (x) => this.fail(x)
    });
  }
  createParticipant() {
    if (!this.participantName.trim()) return;
    this.service.createParticipant(this.participantName.trim()).subscribe({
      next: (p) => {
        this.participants.update((v) => [...v, p]);
        this.selectedParticipant = p.id;
        this.participantName = '';
      },
      error: (x) => this.fail(x)
    });
  }
  addParticipant() {
    const e = this.selected(),
      org = this.auth.idUsuario();
    if (!e || !org || !this.selectedParticipant) return;
    this.service.addParticipant(e.id, this.selectedParticipant, org).subscribe({
      next: () => this.message.set('Participante agregado'),
      error: (x) => this.fail(x)
    });
  }
  loadReport() {
    const e = this.selected(),
      org = this.auth.idUsuario();
    if (!e || !org) return;
    this.service
      .report(e.id, org)
      .subscribe({ next: (v) => this.report.set(v), error: (x) => this.fail(x) });
  }
  private resetForm() {
    this.nombre = '';
    this.descripcion = '';
    this.fechaHora = '';
    this.idLugar = null;
    this.tipo = 'RECITAL';
    this.demanda = 'BAJA';
  }
  private clearStatus() {
    this.error.set(null);
    this.message.set(null);
  }
  private fail(e: HttpErrorResponse) {
    this.error.set(typeof e.error === 'string' ? e.error : 'No se pudo completar la operacion');
  }
}
