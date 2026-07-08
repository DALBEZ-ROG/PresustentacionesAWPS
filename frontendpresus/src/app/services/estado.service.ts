import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, timer, switchMap, distinctUntilChanged, shareReplay, EMPTY } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class EstadoService {
  private api = 'http://localhost:8080/api/estado';

  constructor(private http: HttpClient) {}

  /** RF-03: Estado completo de una solicitud (para polling) */
  estadoSolicitud(id: number): Observable<any> {
    return this.http.get(`${this.api}/solicitud/${id}`);
  }

  /**
   * RF-03: Observable que hace polling cada `intervalMs` ms.
   * Se detiene automáticamente si no hay token (sesión cerrada).
   */
  pollingEstado(id: number, intervalMs = 15000): Observable<any> {
    return timer(0, intervalMs).pipe(
      switchMap(() => {
        const token = localStorage.getItem('presus_token');
        if (!token) return EMPTY;
        return this.estadoSolicitud(id);
      }),
      distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
      shareReplay(1)
    );
  }

  estadoBatch(ids: number[]): Observable<any> {
    return this.http.post(`${this.api}/solicitudes/batch`, ids);
  }
}
