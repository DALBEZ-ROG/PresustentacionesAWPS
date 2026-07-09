import { Component, ViewEncapsulation, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { RubricaEvaluacionService, EvaluacionRubricaResponse } from '../../../services/rubrica-evaluacion.service';
import { JuryEvaluationService } from '../../../services/jury-evaluation.service';
import { SolicitudService } from '../../../services/solicitud.service';

@Component({
    encapsulation: ViewEncapsulation.None,
    selector: 'app-ver-rubrica-tribunal',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './ver-rubrica-tribunal.component.html',
    styleUrls: ['./ver-rubrica-tribunal.component.css']
})
export class VerRubricaTribunalComponent implements OnInit {
    solicitudId!: number;
    solicitud: any = null;
    evaluaciones: EvaluacionRubricaResponse[] = [];
    evaluacionesJurado: any[] = [];
    cargando = true;
    usandoNotaDirecta = false;

    readonly ESCALAS_LABEL: Record<number, string> = {
        100: 'Completo',
        67:  'Casi completo',
        33:  'Significativamente incompleto',
        0:   'No presenta/entrega',
    };

    constructor(
        private route: ActivatedRoute,
        private rubricaEvalService: RubricaEvaluacionService,
        private juryEvalService: JuryEvaluationService,
        private solicitudService: SolicitudService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.solicitudId = Number(this.route.snapshot.paramMap.get('id'));
        this.solicitudService.obtenerPorId(this.solicitudId).subscribe(s => {
            this.solicitud = s;
            this.cdr.markForCheck();
        });

        // Intentar cargar evaluaciones de rubrica
        this.rubricaEvalService.obtenerEvaluacionesSolicitud(this.solicitudId).subscribe({
            next: (evals) => {
                if (evals && evals.length > 0 && evals.some(e => e.notaTotalJurado !== null)) {
                    this.evaluaciones = evals;
                    this.cargando = false;
                } else {
                    // Si no hay rubrica, cargar evaluaciones directas
                    this.cargarEvaluacionesDirectas();
                }
                this.cdr.markForCheck();
            },
            error: () => {
                this.cargarEvaluacionesDirectas();
            }
        });
        setTimeout(() => { if (this.cargando) { this.cargando = false; this.cdr.markForCheck(); } }, 10000);
    }

    cargarEvaluacionesDirectas(): void {
        this.juryEvalService.obtenerTribunal(this.solicitudId).subscribe({
            next: (evals) => {
                this.evaluacionesJurado = evals;
                this.usandoNotaDirecta = true;
                this.cargando = false;
                this.cdr.markForCheck();
            },
            error: () => { this.cargando = false; this.cdr.markForCheck(); }
        });
    }

    get evaluadosCount(): number {
        if (this.usandoNotaDirecta) return this.evaluacionesJurado.filter(e => e.notaJurado).length;
        return this.evaluaciones.filter(e => e.notaTotalJurado !== null).length;
    }

    get totalJurados(): number {
        if (this.usandoNotaDirecta) return this.evaluacionesJurado.length;
        return this.evaluaciones.length;
    }

    get notaPromedio(): number | null {
        if (this.usandoNotaDirecta) {
            const notas = this.evaluacionesJurado.filter(e => e.notaJurado).map(e => e.notaJurado);
            if (notas.length === 0) return null;
            return Math.round(notas.reduce((a: number, b: number) => a + b, 0) / notas.length * 100) / 100;
        }
        const notas = this.evaluaciones.filter(e => e.notaTotalJurado !== null).map(e => e.notaTotalJurado!);
        if (notas.length === 0) return null;
        return Math.round(notas.reduce((a, b) => a + b, 0) / notas.length * 100) / 100;
    }

    get tribunalCompleto(): boolean {
        if (this.usandoNotaDirecta) return this.evaluacionesJurado.length > 0 && this.evaluacionesJurado.every(e => e.notaJurado);
        return this.evaluaciones.length > 0 && this.evaluaciones.every(e => e.notaTotalJurado !== null);
    }

    formatRol(rol: string): string {
        const map: Record<string, string> = { PRESIDENTE: 'Presidente', VOCAL_1: 'Vocal 1', VOCAL_2: 'Vocal 2', TUTOR: 'Tutor' };
        return map[rol] || rol;
    }

    getLabelEscala(escala: number): string {
        return this.ESCALAS_LABEL[escala] ?? String(escala) + '%';
    }
}
