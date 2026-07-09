import { Component, ViewEncapsulation, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';

@Component({
    encapsulation: ViewEncapsulation.None,
    selector: 'app-perfil',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './perfil.component.html',
    styleUrls: ['./perfil.component.css']
})
export class PerfilComponent implements OnInit {
    usuario: any = null;
    nombre = '';
    apellido = '';
    email = '';
    emailNotificaciones = '';
    telefono = '';
    passwordActual = '';
    passwordNueva = '';
    passwordConfirm = '';
    guardando = false;
    cargando = true;
    emailInvalido = false;
    modoEdicion = false;

    private apiUrl = 'http://localhost:8080/api/usuarios';

    constructor(
        private http: HttpClient,
        private authService: AuthService,
        private notification: NotificationService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.cargarPerfil();
        setTimeout(() => { if (this.cargando) { this.cargando = false; this.cdr.markForCheck(); } }, 10000);
    }

    cargarPerfil(): void {
        const id = this.authService.getUserId();
        this.http.get<any>(`${this.apiUrl}/${id}`).subscribe({
            next: (u) => {
                this.usuario = u;
                this.nombre = u.nombre || '';
                this.apellido = u.apellido || '';
                this.email = u.email || '';
                this.emailNotificaciones = u.emailNotificaciones || '';
                this.telefono = u.telefono || '';
                this.cargando = false;
                this.cdr.markForCheck();
            },
            error: () => {
                this.notification.error('No se pudo cargar el perfil.', 'Error');
                this.cargando = false;
                this.cdr.markForCheck();
            }
        });
    }

    validarEmail(): boolean {
        if (!this.emailNotificaciones) { this.emailInvalido = false; return true; }
        const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        this.emailInvalido = !regex.test(this.emailNotificaciones);
        return !this.emailInvalido;
    }

    guardar(): void {
        if (!this.validarEmail()) {
            this.notification.error('Ingresa un correo electrónico válido.', 'Correo inválido');
            return;
        }
        if (this.passwordNueva && this.passwordNueva !== this.passwordConfirm) {
            this.notification.error('Las contrasenas no coinciden.', 'Error');
            return;
        }
        if (this.passwordNueva && !this.passwordActual) {
            this.notification.error('Ingresa tu contrasena actual para poder cambiarla.', 'Error');
            return;
        }

        this.guardando = true;
        const id = this.authService.getUserId();
        const emailCambio = this.email !== this.usuario.email;

        const body: any = {
            nombre: this.nombre,
            apellido: this.apellido,
            email: this.email,
            emailNotificaciones: this.emailNotificaciones,
            telefono: this.telefono
        };
        if (this.passwordNueva) {
            body.passwordActual = this.passwordActual;
            body.passwordNueva = this.passwordNueva;
        }

        this.http.patch(`${this.apiUrl}/${id}/perfil`, body).subscribe({
            next: (u: any) => {
                this.usuario = u;
                this.authService.marcarEmailNotiConfigurado();
                this.guardando = false;
                this.modoEdicion = false;
                this.passwordActual = '';
                this.passwordNueva = '';
                this.passwordConfirm = '';

                if (emailCambio || this.passwordNueva) {
                    this.notification.success('Perfil actualizado. Se cerrara la sesion para aplicar los cambios de seguridad.', '✓ Guardado');
                    setTimeout(() => { this.authService.logout(); }, 2000);
                } else {
                    this.notification.success('Perfil actualizado correctamente.', '✓ Guardado');
                }
                this.cdr.markForCheck();
            },
            error: (err) => {
                const msg = err?.error?.error || 'No se pudo actualizar el perfil.';
                this.notification.error(msg, 'Error');
                this.guardando = false;
                this.cdr.markForCheck();
            }
        });
    }

    activarEdicion(): void {
        this.modoEdicion = true;
    }

    cancelarEdicion(): void {
        this.modoEdicion = false;
        this.nombre = this.usuario?.nombre || '';
        this.apellido = this.usuario?.apellido || '';
        this.email = this.usuario?.email || '';
        this.emailNotificaciones = this.usuario?.emailNotificaciones || '';
        this.telefono = this.usuario?.telefono || '';
        this.passwordActual = '';
        this.passwordNueva = '';
        this.passwordConfirm = '';
        this.emailInvalido = false;
    }

    get rolLabel(): string {
        const map: Record<string, string> = {
            ADMIN: 'Coordinador',
            DOCENTE: 'Docente / Jurado',
            ESTUDIANTE: 'Estudiante'
        };
        return map[this.authService.getRole()] || this.authService.getRole();
    }
}
