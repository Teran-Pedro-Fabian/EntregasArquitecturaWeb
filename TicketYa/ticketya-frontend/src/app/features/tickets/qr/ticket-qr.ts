import { Component, effect, input, signal } from '@angular/core';
import * as QRCode from 'qrcode';

@Component({
  selector: 'app-ticket-qr',
  templateUrl: './ticket-qr.html',
  styleUrl: './ticket-qr.scss'
})
export class TicketQr {
  readonly value = input.required<string>();
  readonly imageUrl = signal('');

  constructor() {
    effect(() => {
      const value = this.value();
      this.imageUrl.set('');

      void QRCode.toString(value, {
        type: 'svg',
        margin: 1,
        width: 220,
        errorCorrectionLevel: 'M'
      }).then((svg) => {
        this.imageUrl.set(`data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`);
      });
    });
  }
}
