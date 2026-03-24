# AGENTS.md - Sistema de Pre-Sustentaciones UTEQ

## Descripción del Proyecto

Sistema integral para la gestión de pre-sustentaciones de trabajos de titulación en la Universidad Técnica Estatal de Quevedo.

- **Backend**: Spring Boot 3.2.1 + Java 17
- **Frontend**: Angular 21
- **Base de datos**: PostgreSQL
- **Autenticación**: Spring Security + JWT

---

## Comandos de Build, Lint y Test

### Backend (Spring Boot)

```bash
# Compilar proyecto
mvn clean install

# Ejecutar aplicación
mvn spring-boot:run

# Ejecutar todos los tests
mvn test

# Ejecutar un test específico
mvn test -Dtest=NombreClaseTest

# Ejecutar tests con cobertura
mvn test jacoco:report

# Solo compilar sin ejecutar
mvn compile
```

### Frontend (Angular)

```bash
# Instalar dependencias
npm install

# Iniciar servidor de desarrollo (http://localhost:4200)
npm run start

# Compilar para producción
npm run build

# Compilar en modo watch
npm run build --watch --configuration development

# Ejecutar tests unitarios
npm run test

# Ejecutar un test específico
ng test --include=**/archivo.spec.ts

# Ejecutar tests con watch mode
ng test --watch
```

---

## Convenciones de Código - Backend (Java)

### Estructura de Paquetes

```
ec.edu.uteq.presustentaciones/
├── config/              # Configuraciones (@Configuration)
├── controllers/         # Controladores REST (@RestController)
├── dto/                 # Data Transfer Objects
├── entities/            # Entidades JPA (@Entity)
├── enums/               # Enumeraciones
├── exceptions/          # Manejo de excepciones
├── repositories/       # Repositorios JPA (extends JpaRepository)
├── security/           # Seguridad JWT
│   ├── jwt/             # JwtTokenProvider, JwtAuthenticationFilter
│   ├── service/          # CustomUserDetailsService
│   └── dto/             # LoginRequest, LoginResponse
└── services/            # Lógica de negocio (interfaces + impl)
```

### Convenciones de Nomenclatura

| Elemento | Convención | Ejemplo |
|----------|------------|---------|
| Paquetes | minúsculas | `ec.edu.uteq.presustentaciones.services` |
| Clases | PascalCase | `UsuarioServiceImpl` |
| Interfaces | PascalCase con prefijo I | `IUsuarioService` |
| Métodos | camelCase | `obtenerPorId()` |
| Variables | camelCase | `usuarioRepository` |
| Constantes | UPPER_SNAKE_CASE | `MAX_REINTENTOS` |

### Patrones de Implementación

#### Repositorios
```java
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Usuario> findByActivoTrue();
}
```

#### Servicios
```java
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public Usuario crear(Usuario usuario) {
        log.info("Creando usuario con email: {}", usuario.getEmail());
        // lógica...
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }
}
```

#### Controladores REST
```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        // lógica...
    }
}
```

### Anotaciones Lombok

| Anotación | Uso |
|-----------|-----|
| `@Service` | Marcar clases de servicio |
| `@Repository` | Marcar repositorios |
| `@RestController` | Controladores REST |
| `@RequiredArgsConstructor` | Genera constructor con dependencias finales |
| `@Slf4j` | Logger `log.info()`, `log.error()` |
| `@Transactional` | Transaccionalidad por defecto |
| `@Transactional(readOnly = true)` | Solo lectura |
| `@Valid` | Validación de beans |

### Manejo de Errores

```java
// Lanzar excepciones
throw new RuntimeException("Usuario no encontrado con ID: " + id);

// En controladores
@PostMapping
public ResponseEntity<?> crear(@Valid @RequestBody DTO dto) {
    // usar Optional para valores que pueden ser nulos
}
```

---

## Convenciones de Código - Frontend (Angular/TypeScript)

### Estructura de Directorios

```
src/app/
├── components/           # Componentes por funcionalidad
│   ├── auth/
│   ├── admin/
│   ├── dashboard/
│   └── [demas]/
├── services/            # Servicios HTTP (API)
├── models/              # Interfaces y tipos
├── guards/              # Guards de ruta
├── interceptors/        # Interceptores HTTP
└── [demas]/
```

### Convenciones de Nomenclatura

| Elemento | Convención | Ejemplo |
|----------|------------|---------|
| Archivos | kebab-case | `auth.service.ts` |
| Clases | PascalCase | `AuthService` |
| Métodos | camelCase | `getToken()` |
| Variables | camelCase | `isLoggedIn` |
| Constantes | camelCase | `API_URL` |

### Patrones de Implementación

#### Servicios
```typescript
@Injectable({ providedIn: 'root' })
export class AuthService {
    private apiUrl = 'http://localhost:8080/api/auth';

    constructor(private http: HttpClient, private router: Router) {}

    login(credentials: { email: string; password: string }): Observable<any> {
        return this.http.post(`${this.apiUrl}/login`, credentials).pipe(
            tap((res: any) => {
                localStorage.setItem('presus_token', res.token);
            })
        );
    }
}
```

#### Componentes Standalone
```typescript
@Component({
    encapsulation: ViewEncapsulation.None,
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './login.component.html',
    styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
    loginForm!: FormGroup;

    constructor(private fb: FormBuilder, private authService: AuthService) {}

    ngOnInit(): void {
        this.loginForm = this.fb.group({
            email: ['', [Validators.required, Validators.email]],
            password: ['', [Validators.required, Validators.minLength(4)]]
        });
    }
}
```

### Importaciones

```typescript
// Imports relativos para la misma aplicación
import { AuthService } from '../../../services/auth.service';

// Angular core
import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

// Módulos comunes
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
```

### Configuración Prettier (package.json)

```json
"prettier": {
    "printWidth": 100,
    "singleQuote": true
}
```

---

## Reglas Generales

### Validación de Entradas
- Backend: Usar `@Valid` en endpoints POST/PUT
- Frontend: Usar `Validators.required`, `Validators.email`, etc.

### Autenticación JWT
- Token almacenado en `localStorage` como `presus_token`
- Headers: `Authorization: Bearer {token}`
- Interceptor HTTP para agregar token automáticamente

### URLs de API
- Backend: `http://localhost:8080/api`
- Frontend: `http://localhost:4200`

### Base de Datos
- PostgreSQL
- Database: `presusDb`
- Usuario: `postgres`
- Password: `postgreAdmin19`

---

## Estados de Solicitud

### Flujo de Transiciones

```
CREADA → ENVIADA → APROBADA → TUTORIA → EVALUACION → CALIFICADA → COMPLETADA
                   ↓            ↓            ↓             ↓
              RECHAZADA    RECHAZADA     RECHAZADA      RECHAZADA
                   ↓            ↓            ↓             ↓
SUSPENDIDA ← Puede suspenderse desde cualquier estado activo
                                              ↓
                                          COMPLETADA
                                      (Estado final - no suspendible)
```

### Estados Disponibles

| Estado | Descripción | Trigger |
|--------|-------------|---------|
| `CREADA` | Solicitud creada por el estudiante | Creación |
| `ENVIADA` | Enviada a revisión | Estudiante envía |
| `APROBADA` | Aprobada por admin/docente | Admin aprueba |
| `RECHAZADA` | Rechazada | Admin/Docente rechaza |
| `SUSPENDIDA` | Suspendida por coordinador | Coordinador suspende (cualquier fase) |
| `TUTORIA` | Tutor asignado | Se asigna tutor |
| `EVALUACION` | Tribunal asignado | Se asignan jurados |
| `CALIFICADA` | Evaluada | Se registra evaluación |
| `COMPLETADA` | Acta firmada | Todos firman el acta |

### Reglas de Suspensión

- El coordinador puede suspender la solicitud desde **cualquier estado activo**
- No se puede suspender: `COMPLETADA`, `SUSPENDIDA`, `RECHAZADA`

### Enum Backend

```java
// Backendpresus/backend/backend-mod/src/main/java/ec/edu/uteq/presustentaciones/enums/EstadoSolicitud.java
public enum EstadoSolicitud {
    CREADA,
    ENVIADA,
    APROBADA,
    RECHAZADA,
    SUSPENDIDA,
    TUTORIA,
    EVALUACION,
    CALIFICADA,
    COMPLETADA;

    public boolean esSuspendible() {
        return this != COMPLETADA && this != SUSPENDIDA && this != RECHAZADA;
    }
}
```

### Badges Frontend

| Estado | Badge CSS | Color |
|--------|-----------|-------|
| CREADA | `badge-creada` | Amarillo |
| ENVIADA | `badge-enviada` | Azul |
| APROBADA | `badge-aprobada` | Verde |
| RECHAZADA | `badge-rechazada` | Rojo |
| SUSPENDIDA | `badge-suspendida` | Naranja |
| TUTORIA | `badge-tutoria` | Índigo |
| EVALUACION | `badge-evaluacion` | Púrpura |
| CALIFICADA | `badge-calificada` | Verde oscuro |
| COMPLETADA | `badge-completada` | Teal |

---

## Recursos Adicionales

- Swagger UI: `http://localhost:8080/api/swagger-ui.html`
- Documentación: `Backendpresus/backend/backend-mod/INSTRUCCIONES.md`
