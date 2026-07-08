package ec.edu.uteq.presustentaciones.controllers;

import ec.edu.uteq.presustentaciones.entities.Evaluacion;
import ec.edu.uteq.presustentaciones.services.EvaluacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/evaluaciones")
public class EvaluacionController {

    private final EvaluacionService evaluacionService;

    public EvaluacionController(EvaluacionService evaluacionService) {
        this.evaluacionService = evaluacionService;
    }

    /**
     * RF-09: Registrar evaluación con notas de instructor y jurado.
     * Los pesos y nota final se calculan desde la modalidad (tabla normalizada).
     */
    @PostMapping("/evaluar")
    public ResponseEntity<?> evaluar(
            @RequestParam Long solicitudId,
            @RequestParam Long rubricaId,
            @RequestParam(required = false) Double notaInstructor,
            @RequestParam(required = false) Double notaJurado,
            @RequestParam(required = false, defaultValue = "") String observaciones) {
        try {
            Evaluacion e = evaluacionService.evaluarSolicitud(
                    solicitudId, rubricaId,
                    notaInstructor, notaJurado,
                    observaciones);
            return ResponseEntity.ok(e);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping
    public List<Evaluacion> listar() {
        return evaluacionService.listarEvaluaciones();
    }

    @GetMapping("/estudiante/{estudianteId}")
    public List<Evaluacion> listarPorEstudiante(@PathVariable Long estudianteId) {
        return evaluacionService.listarPorEstudiante(estudianteId);
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<Evaluacion> listarPorUsuario(@PathVariable Long usuarioId) {
        return evaluacionService.listarPorUsuario(usuarioId);
    }

    @GetMapping("/solicitud/{solicitudId}")
    public ResponseEntity<?> porSolicitud(@PathVariable Long solicitudId) {
        return evaluacionService.buscarPorSolicitud(solicitudId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok().build());
    }
}
