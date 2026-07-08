import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class EvaluacionService {
  private api = 'http://localhost:8080/api/evaluaciones';

  constructor(private http: HttpClient) {}

  /**
   * RF-09: Evaluar con notas de instructor y jurado.
   * Los pesos y nota final se calculan desde la modalidad (BD normalizada).
   */
  evaluar(
    solicitudId: number,
    rubricaId: number,
    notaInstructor: number,
    notaJurado: number,
    observaciones: string
  ): Observable<any> {
    const params = new HttpParams()
      .set('solicitudId', solicitudId)
      .set('rubricaId', rubricaId)
      .set('notaInstructor', notaInstructor)
      .set('notaJurado', notaJurado)
      .set('observaciones', observaciones);
    return this.http.post(`${this.api}/evaluar`, {}, { params });
  }

  listarPorEstudiante(id: number): Observable<any[]> { return this.http.get<any[]>(`${this.api}/estudiante/${id}`); }
  listarPorUsuario(id: number): Observable<any[]>    { return this.http.get<any[]>(`${this.api}/usuario/${id}`); }
  listar(): Observable<any[]>                         { return this.http.get<any[]>(this.api); }
  porSolicitud(id: number): Observable<any>           { return this.http.get(`${this.api}/solicitud/${id}`); }
}
