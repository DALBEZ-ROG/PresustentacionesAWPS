package ec.edu.uteq.presustentaciones.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "acta_firma",
       uniqueConstraints = @UniqueConstraint(columnNames = {"acta_id", "rol_firmante"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActaFirma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acta_id", nullable = false)
    @JsonIgnore
    private Acta acta;

    /** Roles: PRESIDENTE, VOCAL_1, VOCAL_2, TUTOR */
    @Column(name = "rol_firmante", nullable = false, length = 20)
    private String rolFirmante;

    @Column(name = "firmada", nullable = false)
    @Builder.Default
    private boolean firmada = false;

    @Column(name = "fecha_firma")
    private LocalDateTime fechaFirma;
}
