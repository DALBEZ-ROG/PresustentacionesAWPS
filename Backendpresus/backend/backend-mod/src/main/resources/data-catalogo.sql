-- ═══════════════════════════════════════════════════════════════════════════════
-- DATOS CATÁLOGO INICIALES - Sistema Pre-Sustentaciones UTEQ
-- Se ejecuta automáticamente al arrancar con spring.sql.init.mode=always
-- ═══════════════════════════════════════════════════════════════════════════════

-- Roles de usuario
INSERT INTO rol_usuario (codigo, descripcion) VALUES ('ADMIN', 'Administrador del sistema') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO rol_usuario (codigo, descripcion) VALUES ('DOCENTE', 'Docente') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO rol_usuario (codigo, descripcion) VALUES ('ESTUDIANTE', 'Estudiante') ON CONFLICT (codigo) DO NOTHING;

-- Roles de jurado
INSERT INTO rol_jurado (codigo, descripcion) VALUES ('PRESIDENTE', 'Presidente del tribunal') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO rol_jurado (codigo, descripcion) VALUES ('VOCAL_1', 'Primer vocal') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO rol_jurado (codigo, descripcion) VALUES ('VOCAL_2', 'Segundo vocal') ON CONFLICT (codigo) DO NOTHING;

-- Estados de solicitud
INSERT INTO estado_solicitud (codigo) VALUES ('CREADA') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO estado_solicitud (codigo) VALUES ('ENVIADA') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO estado_solicitud (codigo) VALUES ('APROBADA') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO estado_solicitud (codigo) VALUES ('RECHAZADA') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO estado_solicitud (codigo) VALUES ('SUSPENDIDA') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO estado_solicitud (codigo) VALUES ('TUTORIA') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO estado_solicitud (codigo) VALUES ('EVALUACION') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO estado_solicitud (codigo) VALUES ('CALIFICADA') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO estado_solicitud (codigo) VALUES ('COMPLETADA') ON CONFLICT (codigo) DO NOTHING;

-- Estados de anteproyecto
INSERT INTO estado_anteproyecto (codigo) VALUES ('ENVIADO') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO estado_anteproyecto (codigo) VALUES ('APROBADO') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO estado_anteproyecto (codigo) VALUES ('RECHAZADO') ON CONFLICT (codigo) DO NOTHING;

-- Estados de cronograma
INSERT INTO estado_cronograma (codigo) VALUES ('ACTIVO') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO estado_cronograma (codigo) VALUES ('CANCELADO') ON CONFLICT (codigo) DO NOTHING;

-- Estados de fase de tutoría
INSERT INTO estado_fase (codigo) VALUES ('PENDIENTE_ESTUDIANTE') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO estado_fase (codigo) VALUES ('PENDIENTE_TUTOR') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO estado_fase (codigo) VALUES ('APROBADA') ON CONFLICT (codigo) DO NOTHING;

-- Estados de tutor
INSERT INTO estado_tutor (codigo) VALUES ('ACTIVO') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO estado_tutor (codigo) VALUES ('COMPLETADA') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO estado_tutor (codigo) VALUES ('FINALIZADO') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO estado_tutor (codigo) VALUES ('REEMPLAZADO') ON CONFLICT (codigo) DO NOTHING;

-- Tipos de mensaje de tutoría
INSERT INTO tipo_mensaje (codigo) VALUES ('OBSERVACION') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO tipo_mensaje (codigo) VALUES ('RESPUESTA') ON CONFLICT (codigo) DO NOTHING;
INSERT INTO tipo_mensaje (codigo) VALUES ('APROBACION') ON CONFLICT (codigo) DO NOTHING;

-- Modalidades de titulación
INSERT INTO modalidad (codigo, nombre, peso_instructor, peso_jurado, nota_minima_aprobacion) VALUES ('PROYECTO_INVESTIGACION', 'Proyecto de investigación', 60.00, 40.00, 7.00) ON CONFLICT (codigo) DO NOTHING;
INSERT INTO modalidad (codigo, nombre, peso_instructor, peso_jurado, nota_minima_aprobacion) VALUES ('ARTICULO_CIENTIFICO', 'Artículo científico', 60.00, 40.00, 7.00) ON CONFLICT (codigo) DO NOTHING;
INSERT INTO modalidad (codigo, nombre, peso_instructor, peso_jurado, nota_minima_aprobacion) VALUES ('EXAMEN_COMPLEXIVO', 'Examen de Grado (Complexivo)', 50.00, 50.00, 7.00) ON CONFLICT (codigo) DO NOTHING;

-- Rúbrica base de evaluación
INSERT INTO rubricas (id, nombre, descripcion, puntaje_maximo) VALUES (1, 'Rúbrica Pre-Sustentación TIC II', 'Rúbrica estándar para evaluación de pre-sustentaciones', 10.0) ON CONFLICT (id) DO NOTHING;
INSERT INTO criterios_rubrica (rubrica_id, nombre, descripcion, ponderacion, orden, escala) VALUES (1, 'Propuesta y Objetivos', 'Claridad y pertinencia de la propuesta', 33.33, 1, 100) ON CONFLICT DO NOTHING;
INSERT INTO criterios_rubrica (rubrica_id, nombre, descripcion, ponderacion, orden, escala) VALUES (1, 'Documento Escrito', 'Calidad del documento técnico', 33.33, 2, 100) ON CONFLICT DO NOTHING;
INSERT INTO criterios_rubrica (rubrica_id, nombre, descripcion, ponderacion, orden, escala) VALUES (1, 'Exposición Oral', 'Dominio del tema y capacidad de defensa', 33.34, 3, 100) ON CONFLICT DO NOTHING;
