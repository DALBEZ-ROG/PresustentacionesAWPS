package ec.edu.uteq.presustentaciones.services;

import ec.edu.uteq.presustentaciones.entities.Evaluacion;
import ec.edu.uteq.presustentaciones.entities.Rubrica;
import ec.edu.uteq.presustentaciones.entities.Solicitud;
import ec.edu.uteq.presustentaciones.enums.EstadoSolicitud;
import ec.edu.uteq.presustentaciones.repositories.EvaluacionRepository;
import ec.edu.uteq.presustentaciones.repositories.RubricaRepository;
import ec.edu.uteq.presustentaciones.repositories.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluacionServiceImpl implements EvaluacionService {

    private final EvaluacionRepository evaluacionRepository;
    private final SolicitudRepository solicitudRepository;
    private final RubricaRepository rubricaRepository;
    private final NotificacionService notificacionService;

    @Override
    @Transactional
    public Evaluacion evaluarSolicitud(Long solicitudId, Long rubricaId,
                                       Double notaInstructor, Double notaJurado,
                                       String observaciones) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada: " + solicitudId));
        Rubrica rubrica = rubricaRepository.findById(rubricaId)
                .orElseThrow(() -> new RuntimeException("Rúbrica no encontrada: " + rubricaId));

        if (notaInstructor != null && (notaInstructor < 0 || notaInstructor > 10)) {
            throw new RuntimeException("La nota del instructor debe estar entre 0 y 10.");
        }
        if (notaJurado != null && (notaJurado < 0 || notaJurado > 10)) {
            throw new RuntimeException("La nota del jurado debe estar entre 0 y 10.");
        }

        Evaluacion e = Evaluacion.builder()
                .solicitud(solicitud)
                .rubrica(rubrica)
                .notaInstructor(notaInstructor)
                .notaJurado(notaJurado)
                .observaciones(observaciones)
                .comentarioPreestablecido(generarComentarioPorNotas(notaInstructor, notaJurado))
                .build();

        Evaluacion guardada = evaluacionRepository.save(e);

        // Cambiar estado a CALIFICADA
        solicitud.setEstado(EstadoSolicitud.CALIFICADA);
        solicitudRepository.save(solicitud);

        notificarEvaluacion(solicitud, guardada);

        return guardada;
    }

    @Override
    public List<Evaluacion> listarEvaluaciones() {
        return evaluacionRepository.findAll();
    }

    @Override
    public List<Evaluacion> listarPorEstudiante(Long estudianteId) {
        return evaluacionRepository.findByEstudianteId(estudianteId);
    }

    @Override
    public List<Evaluacion> listarPorUsuario(Long usuarioId) {
        return evaluacionRepository.findByUsuarioId(usuarioId);
    }

    @Override
    public Optional<Evaluacion> buscarPorSolicitud(Long solicitudId) {
        return evaluacionRepository.findBySolicitudId(solicitudId);
    }

    public String generarComentarioPorNotas(Double notaInstructor, Double notaJurado) {
        // Promedio simple para generar comentario de orientación
        double promedio = 0;
        int count = 0;
        if (notaInstructor != null) { promedio += notaInstructor; count++; }
        if (notaJurado != null) { promedio += notaJurado; count++; }
        if (count == 0) return "";
        promedio /= count;

        if (promedio <= 3) {
            return "El trabajo no cumple con los requisitos mínimos esperados. Se evidencian falencias significativas que requieren correcciones sustanciales.";
        } else if (promedio <= 6) {
            return "El trabajo presenta un nivel aceptable pero con aspectos que requieren mejoras o correcciones para alcanzar los estándares esperados.";
        } else {
            return "El trabajo cumple satisfactoriamente con los objetivos y requisitos establecidos, demostrando un desempeño adecuado.";
        }
    }

    private void notificarEvaluacion(Solicitud solicitud, Evaluacion evaluacion) {
        try {
            Long usuarioId = solicitud.getEstudiante().getUsuario().getId();
            String titulo = solicitud.getTituloTema();

            String msg = String.format(
                    "📝 Tu pre-sustentación \"%s\" ha sido evaluada. " +
                    "Nota Instructor: %s | Nota Jurado: %s. " +
                    "Tu solicitud ahora está en fase de calificación.",
                    titulo,
                    evaluacion.getNotaInstructor() != null ? String.format("%.2f", evaluacion.getNotaInstructor()) : "Pendiente",
                    evaluacion.getNotaJurado() != null ? String.format("%.2f", evaluacion.getNotaJurado()) : "Pendiente");

            if (evaluacion.getObservaciones() != null && !evaluacion.getObservaciones().isBlank()) {
                msg += " Observaciones: " + evaluacion.getObservaciones();
            }

            notificacionService.crearNotificacion(usuarioId, msg);
        } catch (Exception e) {
            log.warn("No se pudo notificar evaluación al estudiante: {}", e.getMessage());
        }
    }
}
