-- ============================================================================
-- SCHEMA COMPLETO: Sistema de Pre-Sustentaciones UTEQ
-- Ejecutar contra una BD PostgreSQL vacía (ej: presusDb)
-- ============================================================================

-- ═══════════════════════════════════════════════════════════════════════════════
-- 1. TABLAS CATÁLOGO (sin dependencias)
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS rol_usuario (
    codigo VARCHAR(20) PRIMARY KEY,
    descripcion VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS rol_jurado (
    codigo VARCHAR(20) PRIMARY KEY,
    descripcion VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS estado_solicitud (
    codigo VARCHAR(30) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS estado_anteproyecto (
    codigo VARCHAR(30) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS estado_cronograma (
    codigo VARCHAR(30) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS estado_fase (
    codigo VARCHAR(30) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS estado_tutor (
    codigo VARCHAR(30) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS tipo_mensaje (
    codigo VARCHAR(30) PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS modalidad (
    codigo VARCHAR(50) PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    peso_instructor NUMERIC(5,2) NOT NULL,
    peso_jurado NUMERIC(5,2) NOT NULL,
    nota_minima_aprobacion NUMERIC(4,2) NOT NULL
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- 2. TABLAS PRINCIPALES
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL REFERENCES rol_usuario(codigo),
    activo BOOLEAN NOT NULL DEFAULT true,
    telefono VARCHAR(30),
    email_notificaciones VARCHAR(180)
);

CREATE TABLE IF NOT EXISTS estudiante (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE REFERENCES usuarios(id),
    carrera VARCHAR(180) NOT NULL,
    semestre VARCHAR(30),
    expediente_codigo VARCHAR(60) UNIQUE,
    creado_en TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS docente (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE REFERENCES usuarios(id),
    area_especialidad VARCHAR(180),
    carga_horaria_semanal INTEGER NOT NULL DEFAULT 0,
    disponible BOOLEAN NOT NULL DEFAULT true,
    creado_en TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS solicitud (
    id BIGSERIAL PRIMARY KEY,
    estudiante_id BIGINT NOT NULL REFERENCES estudiante(id),
    titulo_tema VARCHAR(300) NOT NULL,
    modalidad VARCHAR(50) REFERENCES modalidad(codigo),
    estado VARCHAR(30) NOT NULL REFERENCES estado_solicitud(codigo),
    observaciones TEXT,
    motivo_suspension TEXT,
    suspendido_en TIMESTAMP,
    creado_por BIGINT REFERENCES usuarios(id),
    actualizado_por BIGINT REFERENCES usuarios(id),
    fecha_registro TIMESTAMP NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS anteproyectos (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL REFERENCES solicitud(id),
    archivo_pdf VARCHAR(255),
    estado VARCHAR(30) REFERENCES estado_anteproyecto(codigo),
    fecha_envio DATE,
    observaciones TEXT,
    sha256_hash VARCHAR(64),
    tamano_bytes BIGINT
);

CREATE TABLE IF NOT EXISTS sala (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    capacidad INTEGER NOT NULL,
    disponible BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE IF NOT EXISTS cronograma (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL UNIQUE REFERENCES solicitud(id),
    sala_id BIGINT NOT NULL REFERENCES sala(id),
    fecha_inicio TIMESTAMP NOT NULL,
    duracion_min INTEGER NOT NULL DEFAULT 45,
    estado VARCHAR(30) NOT NULL REFERENCES estado_cronograma(codigo),
    creado_en TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS tutores (
    id BIGSERIAL PRIMARY KEY,
    docente_id BIGINT NOT NULL REFERENCES docente(id),
    solicitud_id BIGINT NOT NULL REFERENCES solicitud(id),
    fecha_asignacion TIMESTAMP NOT NULL DEFAULT NOW(),
    estado VARCHAR(30) NOT NULL REFERENCES estado_tutor(codigo),
    observaciones TEXT
);

CREATE TABLE IF NOT EXISTS tutoria_fases (
    id BIGSERIAL PRIMARY KEY,
    tutor_id BIGINT NOT NULL REFERENCES tutores(id),
    numero_fase INTEGER NOT NULL,
    estado VARCHAR(30) NOT NULL REFERENCES estado_fase(codigo),
    fecha_inicio TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_aprobacion TIMESTAMP,
    archivo_pdf_estudiante VARCHAR(255),
    sha256_pdf VARCHAR(64),
    tamano_pdf_bytes BIGINT
);

CREATE TABLE IF NOT EXISTS tutoria_mensajes (
    id BIGSERIAL PRIMARY KEY,
    fase_id BIGINT NOT NULL REFERENCES tutoria_fases(id),
    remitente_id BIGINT NOT NULL REFERENCES usuarios(id),
    contenido TEXT NOT NULL,
    fecha_envio TIMESTAMP NOT NULL DEFAULT NOW(),
    tipo VARCHAR(20) NOT NULL REFERENCES tipo_mensaje(codigo),
    leido BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS jurados (
    id BIGSERIAL PRIMARY KEY,
    docente_id BIGINT NOT NULL REFERENCES docente(id),
    solicitud_id BIGINT NOT NULL REFERENCES solicitud(id),
    rol VARCHAR(20) NOT NULL REFERENCES rol_jurado(codigo),
    confirmado BOOLEAN NOT NULL DEFAULT false,
    asignado_en TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS rubricas (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    descripcion TEXT,
    puntaje_maximo DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS criterios_rubrica (
    id BIGSERIAL PRIMARY KEY,
    rubrica_id BIGINT NOT NULL REFERENCES rubricas(id),
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    ponderacion DOUBLE PRECISION NOT NULL,
    orden INTEGER NOT NULL,
    escala INTEGER NOT NULL CHECK (escala > 0)
);

CREATE TABLE IF NOT EXISTS evaluaciones (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT UNIQUE REFERENCES solicitud(id),
    rubrica_id BIGINT REFERENCES rubricas(id),
    nota_instructor DOUBLE PRECISION,
    nota_jurado DOUBLE PRECISION,
    observaciones TEXT,
    comentario_preestablecido TEXT
);

CREATE TABLE IF NOT EXISTS evaluaciones_jurado (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL REFERENCES solicitud(id),
    jurado_id BIGINT NOT NULL REFERENCES jurados(id),
    nota_jurado DOUBLE PRECISION NOT NULL,
    observaciones TEXT,
    comentario_preestablecido TEXT,
    fecha_registro TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS evaluaciones_criterio (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL REFERENCES solicitud(id),
    jurado_id BIGINT NOT NULL REFERENCES jurados(id),
    criterio_id BIGINT NOT NULL REFERENCES criterios_rubrica(id),
    nota_obtenida DOUBLE PRECISION NOT NULL,
    observaciones TEXT,
    observacion_auto TEXT,
    observacion_manual TEXT,
    registrado_en TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (solicitud_id, jurado_id, criterio_id)
);

CREATE TABLE IF NOT EXISTS actas (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL UNIQUE REFERENCES solicitud(id),
    archivo_pdf VARCHAR(255),
    fecha_generacion DATE NOT NULL,
    observaciones_acta TEXT
);

CREATE TABLE IF NOT EXISTS acta_firma (
    id BIGSERIAL PRIMARY KEY,
    acta_id BIGINT NOT NULL REFERENCES actas(id) ON DELETE CASCADE,
    rol_firmante VARCHAR(20) NOT NULL CHECK (rol_firmante IN ('PRESIDENTE', 'TUTOR', 'VOCAL_1', 'VOCAL_2')),
    firmada BOOLEAN NOT NULL DEFAULT false,
    fecha_firma TIMESTAMP,
    UNIQUE (acta_id, rol_firmante)
);

CREATE TABLE IF NOT EXISTS notificaciones (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT REFERENCES usuarios(id),
    mensaje TEXT,
    fecha TIMESTAMP DEFAULT NOW(),
    leida BOOLEAN NOT NULL DEFAULT false
);

-- ═══════════════════════════════════════════════════════════════════════════════
-- 3. VISTAS
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE OR REPLACE VIEW v_evaluaciones AS
SELECT e.id, e.solicitud_id, e.rubrica_id,
       e.nota_instructor, e.nota_jurado,
       m.peso_instructor, m.peso_jurado, m.nota_minima_aprobacion,
       ROUND((e.nota_instructor * m.peso_instructor / 100 + e.nota_jurado * m.peso_jurado / 100)::numeric, 2) AS nota_final,
       CASE WHEN (e.nota_instructor * m.peso_instructor / 100 + e.nota_jurado * m.peso_jurado / 100) >= m.nota_minima_aprobacion
            THEN 'APROBADO' ELSE 'REPROBADO' END AS resultado,
       e.observaciones, e.comentario_preestablecido
FROM evaluaciones e
JOIN solicitud s ON s.id = e.solicitud_id
LEFT JOIN modalidad m ON m.codigo = s.modalidad
WHERE e.nota_instructor IS NOT NULL AND e.nota_jurado IS NOT NULL;

CREATE OR REPLACE VIEW v_evaluaciones_jurado AS
SELECT ej.id, ej.solicitud_id, ej.jurado_id,
       ej.nota_jurado, ej.fecha_registro,
       CASE WHEN ej.nota_jurado >= 7 THEN 'APROBADO' ELSE 'REPROBADO' END AS resultado,
       ej.observaciones, ej.comentario_preestablecido
FROM evaluaciones_jurado ej;

CREATE OR REPLACE VIEW v_actas AS
SELECT a.id, a.solicitud_id, a.archivo_pdf, a.fecha_generacion, a.observaciones_acta,
       (SELECT BOOL_AND(af.firmada) FROM acta_firma af WHERE af.acta_id = a.id) AS firmada,
       (SELECT COUNT(*) FILTER (WHERE af.firmada) FROM acta_firma af WHERE af.acta_id = a.id) AS firmas_completadas,
       (SELECT COUNT(*) FROM acta_firma af WHERE af.acta_id = a.id) AS firmas_requeridas
FROM actas a;

-- ═══════════════════════════════════════════════════════════════════════════════
-- 4. DATOS CATÁLOGO (requeridos para que el sistema funcione)
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO rol_usuario (codigo, descripcion) VALUES
    ('ADMIN', 'Administrador del sistema'),
    ('DOCENTE', 'Docente'),
    ('ESTUDIANTE', 'Estudiante')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO rol_jurado (codigo, descripcion) VALUES
    ('PRESIDENTE', 'Presidente del tribunal'),
    ('VOCAL_1', 'Primer vocal'),
    ('VOCAL_2', 'Segundo vocal')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO estado_solicitud (codigo) VALUES
    ('CREADA'), ('ENVIADA'), ('APROBADA'), ('RECHAZADA'),
    ('SUSPENDIDA'), ('TUTORIA'), ('EVALUACION'), ('CALIFICADA'), ('COMPLETADA')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO estado_anteproyecto (codigo) VALUES
    ('ENVIADO'), ('APROBADO'), ('RECHAZADO')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO estado_cronograma (codigo) VALUES
    ('ACTIVO'), ('CANCELADO')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO estado_fase (codigo) VALUES
    ('PENDIENTE_ESTUDIANTE'), ('PENDIENTE_TUTOR'), ('APROBADA')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO estado_tutor (codigo) VALUES
    ('ACTIVO'), ('COMPLETADA'), ('FINALIZADO'), ('REEMPLAZADO')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO tipo_mensaje (codigo) VALUES
    ('OBSERVACION'), ('RESPUESTA'), ('APROBACION')
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO modalidad (codigo, nombre, peso_instructor, peso_jurado, nota_minima_aprobacion) VALUES
    ('PROYECTO_INVESTIGACION', 'Proyecto de investigación', 60.00, 40.00, 7.00),
    ('ARTICULO_CIENTIFICO', 'Artículo científico', 60.00, 40.00, 7.00),
    ('EXAMEN_COMPLEXIVO', 'Examen de Grado (Complexivo)', 50.00, 50.00, 7.00)
ON CONFLICT (codigo) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════════════
-- 5. USUARIO ADMINISTRADOR INICIAL (password: admin123 - BCrypt)
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO usuarios (nombre, apellido, email, password, rol, activo) VALUES
    ('Coordinador', 'Sistema', 'coordinador@uteq.edu.ec',
     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     'ADMIN', true)
ON CONFLICT (email) DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════════════
-- FIN DEL SCRIPT
-- ═══════════════════════════════════════════════════════════════════════════════
