package ec.edu.uteq.presustentaciones.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "modalidad")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Modalidad {

    @Id
    @Column(name = "codigo", length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "peso_instructor", nullable = false)
    private BigDecimal pesoInstructor;

    @Column(name = "peso_jurado", nullable = false)
    private BigDecimal pesoJurado;

    @Column(name = "nota_minima_aprobacion", nullable = false)
    private BigDecimal notaMinimaAprobacion;
}
