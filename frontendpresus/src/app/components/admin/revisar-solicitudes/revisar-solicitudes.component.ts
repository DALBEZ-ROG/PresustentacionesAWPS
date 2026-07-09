import { Component, ViewEncapsulation, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ReporteService } from '../../../services/reporte.service';
import { SolicitudService } from '../../../services/solicitud.service';
import { NotificationService } from '../../../services/notification.service';
import { JuryEvaluationService } from '../../../services/jury-evaluation.service';
import { EvaluacionService } from '../../../services/evaluacion.service';
import { ActaService } from '../../../services/acta.service';
import Swal from 'sweetalert2';

@Component({
    encapsulation: ViewEncapsulation.None,
    selector: 'app-revisar-solicitudes',
    standalone: true,
    imports: [CommonModule, RouterModule, FormsModule],
    templateUrl: './revisar-solicitudes.component.html',
    styleUrls: ['./revisar-solicitudes.component.css']
})
export class RevisarSolicitudesComponent implements OnInit {
    solicitudes: any[] = [];
    cargando = true;
    filtroEstado = '';
    modalObs: string | null = null;
    modalTitulo = '';

    // Modal de rechazo con observación
    modalRechazo = false;
    solicitudIdRechazo: number | null = null;
    observacionRechazo = '';

    // Modal de suspensión
    modalSuspension = false;
    solicitudIdSuspension: number | null = null;
    motivoSuspension = '';

    constructor(
        private solicitudService: SolicitudService,
        private notification: NotificationService,
        private router: Router,
        private reporteService: ReporteService,
        private juryEvalService: JuryEvaluationService,
        private evaluacionService: EvaluacionService,
        private actaService: ActaService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.cargar();
        // Safety: stop spinner after 10s even if backend unreachable
        setTimeout(() => { if (this.cargando) this.cargando = false; }, 10000);
    }

    cargar(): void {
        this.cargando = true;
        this.solicitudService.listarSolicitudes().subscribe({
            next: (data) => { this.solicitudes = data; this.cargando = false; this.cdr.markForCheck(); },
            error: () => { this.cargando = false; this.notification.error('Error al cargar solicitudes.', 'Error'); this.cdr.markForCheck(); }
        });
    }

    get solicitudesFiltradas(): any[] {
        if (!this.filtroEstado) return this.solicitudes;
        return this.solicitudes.filter(s => s.estado === this.filtroEstado);
    }

    contar(estado: string): number {
        return this.solicitudes.filter(s => s.estado === estado).length;
    }

    setFiltro(estado: string): void { this.filtroEstado = estado; }

    inicialNombre(s: any): string {
        return (s.estudiante?.usuario?.nombre || 'E')[0].toUpperCase();
    }

    aprobar(id: number): void {
        this.solicitudService.aprobarSolicitud(id).subscribe({
            next: () => {
                const sol = this.solicitudes.find(s => s.id === id);
                if (sol) {
                    sol.estado = 'APROBADA';
                    this.cdr.markForCheck();
                }

                this.notification.success(
                    'Solicitud aprobada. Redirigiendo para asignar tribunal...',
                    '✓ Aprobada'
                );

                setTimeout(() => {
                    this.router.navigate(['/dashboard/admin/asignar-jurados', id]);
                }, 1200);
            },
            error: () => this.notification.error('No se pudo aprobar.', 'Error')
        });
    }

    abrirModalRechazo(id: number): void {
        this.solicitudIdRechazo = id;
        this.observacionRechazo = '';
        this.modalRechazo = true;
    }

    cerrarModalRechazo(): void {
        this.modalRechazo = false;
        this.solicitudIdRechazo = null;
        this.observacionRechazo = '';
    }

    confirmarRechazo(): void {
        if (!this.solicitudIdRechazo) return;
        if (!this.observacionRechazo.trim()) {
            this.notification.error('Debes ingresar el motivo del rechazo.', 'Campo requerido');
            return;
        }
        const idArechazar = this.solicitudIdRechazo;
        const obsAGuardar = this.observacionRechazo;

        this.solicitudService.rechazarConObservacion(idArechazar, obsAGuardar).subscribe({
            next: () => {
                this.modalRechazo = false;
                this.solicitudIdRechazo = null;
                this.observacionRechazo = '';

                const sol = this.solicitudes.find(s => s.id === idArechazar);
                if (sol) {
                    sol.estado = 'RECHAZADA';
                    sol.observaciones = obsAGuardar;
                    this.cdr.markForCheck();
                }

                setTimeout(() => {
                    this.notification.success('Solicitud rechazada. El estudiante fue notificado.', '✓ Rechazada');
                }, 100);
            },
            error: () => this.notification.error('No se pudo rechazar.', 'Error')
        });
    }

    abrirModalSuspension(id: number): void {
        this.solicitudIdSuspension = id;
        this.motivoSuspension = '';
        this.modalSuspension = true;
    }

    cerrarModalSuspension(): void {
        this.modalSuspension = false;
        this.solicitudIdSuspension = null;
        this.motivoSuspension = '';
    }

    confirmarSuspension(): void {
        if (!this.solicitudIdSuspension) return;
        if (!this.motivoSuspension.trim()) {
            this.notification.error('Debes ingresar el motivo de la suspensión.', 'Campo requerido');
            return;
        }
        const id = this.solicitudIdSuspension;
        const motivo = this.motivoSuspension;

        this.solicitudService.suspenderSolicitud(id, motivo).subscribe({
            next: () => {
                this.modalSuspension = false;
                this.solicitudIdSuspension = null;
                this.motivoSuspension = '';
                const sol = this.solicitudes.find(s => s.id === id);
                if (sol) {
                    sol.estado = 'SUSPENDIDA';
                    sol.motivoSuspension = motivo;
                    this.cdr.markForCheck();
                }
                this.notification.success('Solicitud suspendida. El estudiante fue notificado.', '✓ Suspendida');
            },
            error: () => this.notification.error('No se pudo suspender.', 'Error')
        });
    }

    verObservaciones(titulo: string, obs: string): void {
        this.modalTitulo = titulo;
        this.modalObs = obs;
    }

    cerrarModal(): void { this.modalObs = null; }

    getBadge(estado: string): string {
        const m: Record<string,string> = {
            CREADA: 'badge-creada',
            ENVIADA: 'badge-enviada',
            APROBADA: 'badge-aprobada',
            RECHAZADA: 'badge-rechazada',
            SUSPENDIDA: 'badge-suspendida',
            TUTORIA: 'badge-tutoria',
            EVALUACION: 'badge-evaluacion',
            CALIFICADA: 'badge-calificada',
            COMPLETADA: 'badge-completada'
        };
        return m[estado] || 'badge-default';
    }

    descargarCronogramaPdf(): void {
        this.reporteService.cronogramaPdf().subscribe({
            next: (blob) => {
                const a = document.createElement('a');
                a.href = URL.createObjectURL(blob);
                a.download = 'cronograma_presustentaciones.pdf';
                a.click();
            },
            error: () => this.notification.error('No se pudo generar el PDF.', 'Error')
        });
    }

    descargarEstadisticasPdf(): void {
        this.reporteService.estadisticasPdf().subscribe({
            next: (blob) => {
                const a = document.createElement('a');
                a.href = URL.createObjectURL(blob);
                a.download = 'estadisticas_evaluaciones.pdf';
                a.click();
            },
            error: () => this.notification.error('No se pudo generar el reporte.', 'Error')
        });
    }

    verObservacionesCompletas(s: any): void {
        const solicitudId = s.id;
        let obsHtml = '';

        // Observacion del coordinador (de la solicitud misma)
        if (s.observaciones) {
            obsHtml += `<div style="text-align:left;margin-bottom:12px"><strong style="color:#003865">Coordinador:</strong><p style="margin:4px 0;padding:8px 12px;background:#f0f6ff;border-radius:8px;border-left:3px solid #003865;font-size:0.9rem">${s.observaciones}</p></div>`;
        }

        // Cargar observaciones de jurados
        this.juryEvalService.obtenerTribunal(solicitudId).subscribe({
            next: (evals) => {
                evals.forEach((ev: any) => {
                    if (ev.observaciones) {
                        const rol = this.formatRolLabel(ev.rolJurado);
                        obsHtml += `<div style="text-align:left;margin-bottom:10px"><strong style="color:#0369a1">${rol} — ${ev.nombreJurado}:</strong><p style="margin:4px 0;padding:8px 12px;background:#f0fdf4;border-radius:8px;border-left:3px solid #16a34a;font-size:0.9rem">${ev.observaciones}</p></div>`;
                    }
                });

                // Cargar observaciones de la evaluacion del coordinador
                this.evaluacionService.porSolicitud(solicitudId).subscribe({
                    next: (evalCoord: any) => {
                        if (evalCoord?.observaciones) {
                            obsHtml += `<div style="text-align:left;margin-bottom:10px"><strong style="color:#003865">Nota del Coordinador:</strong><p style="margin:4px 0;padding:8px 12px;background:#f0f6ff;border-radius:8px;border-left:3px solid #003865;font-size:0.9rem">${evalCoord.observaciones}</p></div>`;
                        }
                        this.mostrarModalObservaciones(s.tituloTema, obsHtml);
                    },
                    error: () => this.mostrarModalObservaciones(s.tituloTema, obsHtml)
                });
            },
            error: () => this.mostrarModalObservaciones(s.tituloTema, obsHtml || '<p style="color:#999">No hay observaciones registradas.</p>')
        });
    }

    private mostrarModalObservaciones(titulo: string, html: string): void {
        Swal.fire({
            title: 'Observaciones',
            html: `<div style="max-height:400px;overflow-y:auto"><p style="font-size:0.82rem;color:#666;margin-bottom:12px">${titulo}</p>${html || '<p style="color:#999">No hay observaciones registradas.</p>'}</div>`,
            width: 600,
            confirmButtonText: 'Cerrar',
            confirmButtonColor: '#003865'
        });
    }

    private formatRolLabel(rol: string): string {
        const map: Record<string,string> = { PRESIDENTE:'Presidente', VOCAL_1:'Vocal 1', VOCAL_2:'Vocal 2', TUTOR:'Tutor' };
        return map[rol] || rol;
    }

    descargarActaSolicitud(solicitudId: number): void {
        this.actaService.porSolicitud(solicitudId).subscribe({
            next: (acta: any) => {
                if (acta && acta.id) {
                    this.actaService.descargarPdf(acta.id).subscribe({
                        next: (blob) => {
                            const url = URL.createObjectURL(blob);
                            const a = document.createElement('a');
                            const sol = this.solicitudes.find(s => s.id === solicitudId);
                            const nombre = sol?.estudiante?.usuario?.nombre || '';
                            const apellido = sol?.estudiante?.usuario?.apellido || '';
                            a.href = url;
                            a.download = `Acta_PreSustentacion_${nombre}_${apellido}.pdf`.replace(/ /g, '_');
                            a.click();
                            URL.revokeObjectURL(url);
                        },
                        error: () => this.notification.error('No se pudo descargar el PDF del acta.', 'Error')
                    });
                } else {
                    this.notification.error('No hay acta generada para esta solicitud. Ingrese a Evaluar para generarla.', 'Sin acta');
                }
            },
            error: () => this.notification.error('No se encontro el acta.', 'Error')
        });
    }
}