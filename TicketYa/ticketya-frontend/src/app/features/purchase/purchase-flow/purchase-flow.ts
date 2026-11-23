import { Component, computed, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PaymentMethod, PublicSeat } from '../data/purchase.models';
import { PurchaseStore } from '../state/purchase.store';

@Component({
  selector: 'app-purchase-flow',
  imports: [RouterLink],
  providers: [PurchaseStore],
  templateUrl: './purchase-flow.html',
  styleUrl: './purchase-flow.scss'
})
export class PurchaseFlow implements OnInit {
  protected readonly store = inject(PurchaseStore);
  private readonly route = inject(ActivatedRoute);

  protected readonly seatRows = computed(() => {
    const rows = new Map<string, PublicSeat[]>();

    for (const seat of this.store.seats()) {
      const current = rows.get(seat.fila) ?? [];
      rows.set(seat.fila, [...current, seat]);
    }

    return Array.from(rows.entries()).map(([row, seats]) => ({ row, seats }));
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) this.store.load(id);
  }

  selectSector(event: Event): void {
    const value = Number((event.target as HTMLSelectElement).value);
    if (Number.isFinite(value)) this.store.selectSector(value);
  }

  changeQuantity(event: Event): void {
    const value = Number((event.target as HTMLInputElement).value);
    if (Number.isFinite(value)) this.store.setQuantity(value);
  }

  changePayment(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;

    if (value === 'TARJETA' || value === 'TRANSFERENCIA' || value === 'BILLETERA_VIRTUAL') {
      this.store.setPaymentMethod(value as PaymentMethod);
    }
  }

  formatPrice(value: number): string {
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS',
      maximumFractionDigits: 0
    }).format(value);
  }

  formatTime(seconds: number): string {
    const minutes = Math.floor(seconds / 60)
      .toString()
      .padStart(2, '0');
    const rest = (seconds % 60).toString().padStart(2, '0');
    return `${minutes}:${rest}`;
  }
}
