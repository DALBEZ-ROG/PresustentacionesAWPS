package ec.edu.uteq.presustentaciones.controllers;

import ec.edu.uteq.presustentaciones.entities.PeriodoAcademico;
import ec.edu.uteq.presustentaciones.repositories.PeriodoAcademicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/periodos")
@RequiredArgsConstructor
public class PeriodoAcademicoController {

    private final PeriodoAcademicoRepository periodoRepository;

    @GetMapping
    public List<PeriodoAcademico> listar() {
        return periodoRepository.findAll();
    }

    @GetMapping("/activo")
    public ResponseEntity<?> obtenerActivo() {
        Optional<PeriodoAcademico> activo = periodoRepository.findByActivoTrue();
        return activo.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(Map.of("mensaje", "No hay periodo activo configurado.")));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, String> body) {
        try {
            // Validar que no exista ya un periodo activo
            if (periodoRepository.findByActivoTrue().isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ya existe un periodo activo. Elimina o desactiva el actual antes de crear uno nuevo."));
            }
            // Validar que no haya ningún periodo registrado (solo puede haber uno)
            if (!periodoRepository.findAll().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Ya existe un periodo configurado. Solo puede haber un periodo a la vez."));
            }

            LocalDate fechaInicio = LocalDate.parse(body.get("fechaInicio"));
            LocalDate fechaFin = LocalDate.parse(body.get("fechaFin"));

            if (fechaFin.isBefore(fechaInicio)) {
                return ResponseEntity.badRequest().body(Map.of("error", "La fecha fin no puede ser anterior a la fecha de inicio."));
            }
            if (fechaFin.isBefore(LocalDate.now())) {
                return ResponseEntity.badRequest().body(Map.of("error", "La fecha fin no puede ser anterior a la fecha actual."));
            }

            // Determinar tipo automáticamente según el mes de inicio
            String tipo = fechaInicio.getMonthValue() <= 6 ? "PPA" : "SPA";

            // Generar nombre automático: REGULAR - AÑO-AÑO+1 PPA/SPA
            int anioInicio = fechaInicio.getYear();
            int anioFin = anioInicio + 1;
            String nombre = "REGULAR - " + anioInicio + "-" + anioFin + " " + tipo;

            PeriodoAcademico periodo = PeriodoAcademico.builder()
                    .nombre(nombre)
                    .tipo(tipo)
                    .fechaInicio(fechaInicio)
                    .fechaFin(fechaFin)
                    .activo(false)
                    .build();
            return ResponseEntity.ok(periodoRepository.save(periodo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al crear periodo: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            PeriodoAcademico periodo = periodoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Periodo no encontrado."));

            if (body.containsKey("fechaInicio")) periodo.setFechaInicio(LocalDate.parse(body.get("fechaInicio")));
            if (body.containsKey("fechaFin")) periodo.setFechaFin(LocalDate.parse(body.get("fechaFin")));

            if (periodo.getFechaFin().isBefore(periodo.getFechaInicio())) {
                return ResponseEntity.badRequest().body(Map.of("error", "La fecha fin no puede ser anterior a la fecha de inicio."));
            }

            // Recalcular tipo y nombre según la nueva fecha de inicio
            String tipo = periodo.getFechaInicio().getMonthValue() <= 6 ? "PPA" : "SPA";
            int anioInicio = periodo.getFechaInicio().getYear();
            int anioFin = anioInicio + 1;
            String nombre = "REGULAR - " + anioInicio + "-" + anioFin + " " + tipo;
            periodo.setTipo(tipo);
            periodo.setNombre(nombre);

            return ResponseEntity.ok(periodoRepository.save(periodo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/activar")
    public ResponseEntity<?> activar(@PathVariable Long id) {
        PeriodoAcademico periodo = periodoRepository.findById(id).orElse(null);
        if (periodo == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Periodo no encontrado."));
        }
        // Desactivar todos los demás
        periodoRepository.findAll().forEach(p -> {
            p.setActivo(false);
            periodoRepository.save(p);
        });
        // Activar este
        periodo.setActivo(true);
        periodoRepository.save(periodo);
        return ResponseEntity.ok(periodo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (!periodoRepository.existsById(id)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Periodo no encontrado."));
        }
        periodoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
