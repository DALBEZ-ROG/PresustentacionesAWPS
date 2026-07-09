import { Component, ViewEncapsulation, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { PeriodoService } from '../../../services/periodo.service';
import { NotificationService } from '../../../services/notification.service';
import Swal from 'sweetalert2';

@Component({
    encapsulation: ViewEncapsulation.None,
    selector: 'app-gestionar-periodos',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule],
    templateUrl: './gestionar-periodos.component.html',
    styleUrls: ['./gestionar-periodos.component.css']
})
export class GestionarPeriodosComponent implements OnInit {
    periodos: any[] = [];
    form!: FormGroup;
    cargando = true;
    enviando = false;
    modoEdicion = false;
    periodoEditandoId: number | null = null;
    hoy = new Date().toISOString().split('T')[0];

    // Preview automático
    previewNombre = '';
    previewTipo = '';

    constructor(
        private fb: FormBuilder,
        private periodoService: PeriodoService,
        private notification: NotificationService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.form = this.fb.group({
            fechaInicio: ['', Validators.required],
            fechaFin: ['', Validators.required]
        });
        this.form.get('fechaInicio')?.valueChanges.subscribe(() => this.actualizarPreview());
        this.cargar();
    }

    actualizarPreview(): void {
        const fechaInicio = this.form.get('fechaInicio')?.value;
        if (fechaInicio) {
            const date = new Date(fechaInicio + 'T00:00:00');
            const mes = date.getMonth() + 1;
            this.previewTipo = mes <= 6 ? 'PPA' : 'SPA';
            const anio = date.getFullYear();
            this.previewNombre = `REGULAR - ${anio}-${anio + 1} ${this.previewTipo}`;
        } else {
            this.previewNombre = '';
            this.previewTipo = '';
        }
    }

    cargar(): void {
        this.cargando = true;
        this.periodoService.listar().subscribe({
            next: (data) => { this.periodos = data; this.cargando = false; this.cdr.markForCheck(); },
            error: () => { this.cargando = false; this.notification.error('Error al cargar periodos.', 'Error'); }
        });
    }

    guardar(): void {
        if (this.form.invalid) return;
        const { fechaInicio, fechaFin } = this.form.value;

        if (fechaFin < fechaInicio) {
            this.notification.error('La fecha fin no puede ser anterior a la fecha de inicio.', 'Error de fechas');
            return;
        }

        this.enviando = true;

        if (this.modoEdicion && this.periodoEditandoId) {
            this.periodoService.actualizar(this.periodoEditandoId, { fechaInicio, fechaFin }).subscribe({
                next: () => {
                    this.notification.success('Periodo actualizado correctamente.', '✓ Actualizado');
                    this.resetForm();
                    this.cargar();
                },
                error: (err) => {
                    this.notification.error(err.error?.error || 'Error al actualizar.', 'Error');
                    this.enviando = false;
                    this.cdr.markForCheck();
                }
            });
        } else {
            this.periodoService.crear({ fechaInicio, fechaFin }).subscribe({
                next: () => {
                    this.notification.success('Periodo creado correctamente.', '✓ Creado');
                    this.resetForm();
                    this.cargar();
                },
                error: (err) => {
                    this.notification.error(err.error?.error || 'Error al crear periodo.', 'Error');
                    this.enviando = false;
                    this.cdr.markForCheck();
                }
            });
        }
    }

    editar(p: any): void {
        this.modoEdicion = true;
        this.periodoEditandoId = p.id;
        this.form.patchValue({
            fechaInicio: p.fechaInicio,
            fechaFin: p.fechaFin
        });
        this.actualizarPreview();
    }

    activar(id: number): void {
        this.periodoService.activar(id).subscribe({
            next: () => {
                this.notification.success('Periodo activado. Las pre-sustentaciones se programarán dentro de este rango.', '✓ Activado');
                this.cargar();
            },
            error: () => this.notification.error('No se pudo activar.', 'Error')
        });
    }

    eliminar(id: number): void {
        Swal.fire({
            title: '¿Eliminar este periodo?',
            text: 'Esta acción no se puede deshacer.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#6c757d',
            confirmButtonText: 'Sí, eliminar',
            cancelButtonText: 'Cancelar'
        }).then((result) => {
            if (result.isConfirmed) {
                this.periodoService.eliminar(id).subscribe({
                    next: () => { this.notification.success('Periodo eliminado.', '✓'); this.cargar(); },
                    error: () => this.notification.error('No se pudo eliminar.', 'Error')
                });
            }
        });
    }

    cancelarEdicion(): void {
        this.resetForm();
    }

    private resetForm(): void {
        this.form.reset();
        this.modoEdicion = false;
        this.periodoEditandoId = null;
        this.enviando = false;
        this.previewNombre = '';
        this.previewTipo = '';
        this.cdr.markForCheck();
    }
}
