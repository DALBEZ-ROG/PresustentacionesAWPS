package ec.edu.uteq.presustentaciones.controllers;

import ec.edu.uteq.presustentaciones.entities.Acta;
import ec.edu.uteq.presustentaciones.services.ActaService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/actas")
public class ActaController {

    private final ActaService actaService;

    public ActaController(ActaService actaService) {
        this.actaService = actaService;
    }

    /** RF-11: Genera el acta con PDF real */
    @PostMapping("/generar/{solicitudId}")
    public ResponseEntity<?> generarActa(@PathVariable Long solicitudId) {
        try {
            Acta acta = actaService.generarActa(solicitudId);
            return ResponseEntity.ok(acta);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * RF-08: Firma el acta por rol específico.
     * rol: PRESIDENTE, VOCAL_1, VOCAL_2, TUTOR
     */
    @PostMapping("/firmar/{actaId}")
    public ResponseEntity<?> firmarActa(
            @PathVariable Long actaId,
            @RequestParam String rol) {
        try {
            Acta acta = actaService.firmarActa(actaId, rol);
            return ResponseEntity.ok(acta);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** RF-11: Descarga el PDF del acta */
    @GetMapping("/descargar/{actaId}")
    public ResponseEntity<?> descargarPdf(@PathVariable Long actaId) {
        try {
            byte[] pdfBytes = actaService.obtenerPdfBytes(actaId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"acta_" + actaId + ".pdf\"")
                    .body(pdfBytes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "El archivo PDF no está disponible. Regenere el acta."));
        }
    }

    /** Vista en línea del PDF (en navegador) */
    @GetMapping("/ver/{actaId}")
    public ResponseEntity<?> verPdf(@PathVariable Long actaId) {
        try {
            byte[] pdfBytes = actaService.obtenerPdfBytes(actaId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"acta_" + actaId + ".pdf\"")
                    .body(pdfBytes);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "El archivo PDF no está disponible. Regenere el acta."));
        }
    }

    @GetMapping
    public List<Acta> listar() {
        return actaService.listarActas();
    }

    @GetMapping("/solicitud/{solicitudId}")
    public ResponseEntity<?> porSolicitud(@PathVariable Long solicitudId) {
        return actaService.buscarPorSolicitud(solicitudId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok().build());
    }
}
