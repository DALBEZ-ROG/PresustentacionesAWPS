import { Component, ViewEncapsulation, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificacionService } from '../../services/notificacion.service';
import { AuthService } from '../../services/auth.service';

@Component({
    encapsulation: ViewEncapsulation.None,
    selector: 'app-notificaciones',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './notificaciones.component.html',
    styleUrls: ['./notificaciones.component.css']
})
export class NotificacionesComponent implements OnInit {
    notificaciones: any[] = [];
    cargando = false;
    usuarioId = 0;

    constructor(
        private notiService: NotificacionService,
        private authService: AuthService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.usuarioId = this.authService.getUserId();
        this.cargar();
        setTimeout(() => { if (this.cargando) { this.cargando = false; this.cdr.markForCheck(); } }, 10000);
    }

    cargar(): void {
        this.cargando = true;
        this.notiService.listarPorUsuario(this.usuarioId).subscribe({
            next: (data) => {
                this.notificaciones = data;
                this.cargando = false;
                this.cdr.markForCheck();
                this.notiService.refrescarBadge(this.usuarioId);
            },
            error: () => { this.cargando = false; this.cdr.markForCheck(); }
        });
    }

    marcarLeida(id: number): void {
        const n = this.notificaciones.find(x => x.id === id);
        if (n && n.leida) return;
        this.notiService.marcarLeida(id).subscribe({
            next: () => { if (n) n.leida = true; this.cdr.markForCheck(); }
        });
    }

    marcarTodas(): void {
        this.notiService.marcarTodasLeidas(this.usuarioId).subscribe({
            next: () => { this.notificaciones.forEach(n => n.leida = true); this.cdr.markForCheck(); }
        });
    }

    get noLeidas(): number {
        return this.notificaciones.filter(n => !n.leida).length;
    }

    getTitulo(mensaje: string): string {
        if (!mensaje) return '';
        // Quitar emojis del inicio
        const limpio = mensaje.replace(/^[\u{1F300}-\u{1FAD6}\u{2600}-\u{27BF}\u{FE00}-\u{FE0F}\u{1F900}-\u{1F9FF}\s]+/u, '');
        // Tomar hasta el primer punto seguido de espacio, o primeros 60 chars
        const puntoIdx = limpio.indexOf('. ');
        const comillaIdx = limpio.indexOf('"');
        if (comillaIdx > 0 && comillaIdx < 50) {
            return limpio.substring(0, comillaIdx).trim();
        }
        if (puntoIdx > 0 && puntoIdx < 80) {
            return limpio.substring(0, puntoIdx + 1);
        }
        if (limpio.length > 60) {
            return limpio.substring(0, 60).trim();
        }
        return limpio;
    }

    getDetalle(mensaje: string): string {
        if (!mensaje) return '';
        const titulo = this.getTitulo(mensaje);
        const limpio = mensaje.replace(/^[\u{1F300}-\u{1FAD6}\u{2600}-\u{27BF}\u{FE00}-\u{FE0F}\u{1F900}-\u{1F9FF}\s]+/u, '');
        const detalle = limpio.substring(titulo.length).trim();
        return detalle || '';
    }

    getIcono(mensaje: string): string {
        if (!mensaje) return 'bi-bell-fill';
        if (mensaje.includes('tutor')) return 'bi-person-check-fill';
        if (mensaje.includes('jurado') || mensaje.includes('tribunal')) return 'bi-people-fill';
        if (mensaje.includes('acta') || mensaje.includes('firmada')) return 'bi-file-earmark-check-fill';
        if (mensaje.includes('programada') || mensaje.includes('cronograma')) return 'bi-calendar-check-fill';
        if (mensaje.includes('aprobada')) return 'bi-check-circle-fill';
        if (mensaje.includes('rechazada')) return 'bi-x-circle-fill';
        if (mensaje.includes('suspendida')) return 'bi-slash-circle-fill';
        return 'bi-bell-fill';
    }
}
