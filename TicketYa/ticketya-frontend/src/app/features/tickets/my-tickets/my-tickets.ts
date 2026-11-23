import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TicketEntry } from '../data/ticket.models';
import { TicketQr } from '../qr/ticket-qr';
import { TicketsStore } from '../state/tickets.store';

@Component({
  selector: 'app-my-tickets',
  imports: [CurrencyPipe, DatePipe, RouterLink, TicketQr],
  providers: [TicketsStore],
  templateUrl: './my-tickets.html',
  styleUrl: './my-tickets.scss'
})
export class MyTicketsPage implements OnInit {
  protected readonly store = inject(TicketsStore);

  ngOnInit(): void {
    this.store.load();
  }

  protected seatLabel(ticket: TicketEntry): string {
    if (ticket.idAsiento !== null) {
      return `Asiento ${ticket.idAsiento}`;
    }

    return 'Entrada general';
  }
}
