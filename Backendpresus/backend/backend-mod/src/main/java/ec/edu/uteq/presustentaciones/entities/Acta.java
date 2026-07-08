package ec.edu.uteq.presustentaciones.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "actas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Acta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDate fechaGeneracion;

    @Column(name = "archivo_pdf")
    private String archivoPdf;

    @Column(name = "observaciones_acta", columnDefinition = "TEXT")
    private String observacionesActa;

    @OneToOne
    @JoinColumn(name = "solicitud_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Solicitud solicitud;

    @OneToMany(mappedBy = "acta", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Builder.Default
    private List<ActaFirma> firmas = new ArrayList<>();

    /** true solo cuando TODOS los firmantes han firmado */
    public boolean isFirmada() {
        if (firmas == null || firmas.isEmpty()) return false;
        return firmas.stream().allMatch(ActaFirma::isFirmada);
    }

    /** Retorna los firmantes pendientes como texto */
    public String getFirmantesPendientes() {
        if (firmas == null || firmas.isEmpty()) return "Sin firmantes asignados";
        StringBuilder sb = new StringBuilder();
        for (ActaFirma firma : firmas) {
            if (!firma.isFirmada()) {
                sb.append(firma.getRolFirmante()).append(", ");
            }
        }
        String result = sb.toString();
        return result.isEmpty() ? "Todos firmaron" : result.substring(0, result.length() - 2);
    }

    public long getFirmasCompletadas() {
        if (firmas == null) return 0;
        return firmas.stream().filter(ActaFirma::isFirmada).count();
    }

    public long getFirmasRequeridas() {
        return firmas == null ? 0 : firmas.size();
    }
}
