import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PeriodoService {
    private apiUrl = 'http://localhost:8080/api/periodos';

    constructor(private http: HttpClient) {}

    listar(): Observable<any[]> {
        return this.http.get<any[]>(this.apiUrl);
    }

    obtenerActivo(): Observable<any> {
        return this.http.get(`${this.apiUrl}/activo`);
    }

    crear(periodo: { fechaInicio: string; fechaFin: string }): Observable<any> {
        return this.http.post(this.apiUrl, periodo);
    }

    actualizar(id: number, datos: { fechaInicio: string; fechaFin: string }): Observable<any> {
        return this.http.put(`${this.apiUrl}/${id}`, datos);
    }

    activar(id: number): Observable<any> {
        return this.http.post(`${this.apiUrl}/${id}/activar`, {});
    }

    eliminar(id: number): Observable<any> {
        return this.http.delete(`${this.apiUrl}/${id}`);
    }
}
