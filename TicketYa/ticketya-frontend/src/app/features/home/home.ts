import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EventSearchResult } from '../events/data/event.models';
import { EventService } from '../events/data/event.service';

@Component({
  selector: 'app-home',
  imports: [RouterLink, DatePipe, CurrencyPipe],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class Home implements OnInit {
  private readonly eventService = inject(EventService);

  readonly events = signal<EventSearchResult[]>([]);
  readonly loading = signal(true);
  readonly error = signal(false);

  ngOnInit(): void {
    this.eventService.search({ ordenarPor: 'fecha' }).subscribe({
      next: (events) => {
        this.events.set(events.slice(0, 3));
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      }
    });
  }
}
