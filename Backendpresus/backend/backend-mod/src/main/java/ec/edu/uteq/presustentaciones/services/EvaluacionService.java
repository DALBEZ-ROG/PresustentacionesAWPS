package ec.edu.uteq.presustentaciones.services;

import ec.edu.uteq.presustentaciones.entities.Evaluacion;

import java.util.List;
import java.util.Optional;

public interface EvaluacionService {

    /** Registra evaluación con notas de instructor y jurado */
    Evaluacion evaluarSolicitud(Long solicitudId, Long rubricaId,
                                 Double notaInstructor, Double notaJurado,
                                 String observaciones);

    List<Evaluacion> listarEvaluaciones();
    List<Evaluacion> listarPorEstudiante(Long estudianteId);
    List<Evaluacion> listarPorUsuario(Long usuarioId);
    Optional<Evaluacion> buscarPorSolicitud(Long solicitudId);
}
