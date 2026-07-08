package ec.edu.uteq.presustentaciones.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "evaluaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /** Nota asignada por el instructor del curso */
    @Column(name = "nota_instructor")
    private Double notaInstructor;

    /** Nota asignada por el tribunal/jurado */
    @Column(name = "nota_jurado")
    private Double notaJurado;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "comentario_preestablecido", columnDefinition = "TEXT")
    private String comentarioPreestablecido;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "solicitud_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "creadoPor", "actualizadoPor"})
    private Solicitud solicitud;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rubrica_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Rubrica rubrica;
}
