import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { EventDetailStore } from '../state/event-detail.store';

@Component({
  selector: 'app-event-detail',
  imports: [RouterLink],
  providers: [EventDetailStore],
  templateUrl: './event-detail.html',
  styleUrl: './event-detail.scss'
})
export class EventDetailPage implements OnInit {
  protected readonly store = inject(EventDetailStore);
  private readonly route = inject(ActivatedRoute);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (!id) {
      return;
    }

    this.store.load(id);
  }

  formatDate(value: string): string {
    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
      return value;
    }

    return new Intl.DateTimeFormat('es-AR', {
      dateStyle: 'full',
      timeStyle: 'short'
    }).format(date);
  }
}
