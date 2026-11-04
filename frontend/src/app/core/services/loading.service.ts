import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LoadingService {
  private contador = 0;
  readonly carregando = signal(false);

  mostrar(): void {
    this.contador++;
    this.carregando.set(true);
  }

  esconder(): void {
    this.contador = Math.max(0, this.contador - 1);
    if (this.contador === 0) {
      this.carregando.set(false);
    }
  }
}
