-- ═══════════════════════════════════════════════════════════════════════════════
-- FK CONSTRAINTS hacia tablas catálogo
-- Se ejecuta DESPUÉS de que Hibernate cree las tablas (defer-datasource-initialization=true)
-- ═══════════════════════════════════════════════════════════════════════════════

-- Solicitud → estado_solicitud
ALTER TABLE solicitud DROP CONSTRAINT IF EXISTS fk_solicitud_estado;
ALTER TABLE solicitud ADD CONSTRAINT fk_solicitud_estado FOREIGN KEY (estado) REFERENCES estado_solicitud(codigo);

-- Solicitud → modalidad
ALTER TABLE solicitud DROP CONSTRAINT IF EXISTS fk_solicitud_modalidad;
ALTER TABLE solicitud ADD CONSTRAINT fk_solicitud_modalidad FOREIGN KEY (modalidad) REFERENCES modalidad(codigo);

-- Usuarios → rol_usuario
ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS fk_usuarios_rol;
ALTER TABLE usuarios ADD CONSTRAINT fk_usuarios_rol FOREIGN KEY (rol) REFERENCES rol_usuario(codigo);

-- Anteproyectos → estado_anteproyecto
ALTER TABLE anteproyectos DROP CONSTRAINT IF EXISTS fk_anteproyecto_estado;
ALTER TABLE anteproyectos ADD CONSTRAINT fk_anteproyecto_estado FOREIGN KEY (estado) REFERENCES estado_anteproyecto(codigo);

-- Cronograma → estado_cronograma
ALTER TABLE cronograma DROP CONSTRAINT IF EXISTS fk_cronograma_estado;
ALTER TABLE cronograma ADD CONSTRAINT fk_cronograma_estado FOREIGN KEY (estado) REFERENCES estado_cronograma(codigo);

-- Tutores → estado_tutor
ALTER TABLE tutores DROP CONSTRAINT IF EXISTS fk_tutor_estado;
ALTER TABLE tutores ADD CONSTRAINT fk_tutor_estado FOREIGN KEY (estado) REFERENCES estado_tutor(codigo);

-- Tutoria_fases → estado_fase
ALTER TABLE tutoria_fases DROP CONSTRAINT IF EXISTS fk_fase_estado;
ALTER TABLE tutoria_fases ADD CONSTRAINT fk_fase_estado FOREIGN KEY (estado) REFERENCES estado_fase(codigo);

-- Tutoria_mensajes → tipo_mensaje
ALTER TABLE tutoria_mensajes DROP CONSTRAINT IF EXISTS fk_mensaje_tipo;
ALTER TABLE tutoria_mensajes ADD CONSTRAINT fk_mensaje_tipo FOREIGN KEY (tipo) REFERENCES tipo_mensaje(codigo);

-- Jurados → rol_jurado
ALTER TABLE jurados DROP CONSTRAINT IF EXISTS fk_jurados_rol;
ALTER TABLE jurados ADD CONSTRAINT fk_jurados_rol FOREIGN KEY (rol) REFERENCES rol_jurado(codigo);
