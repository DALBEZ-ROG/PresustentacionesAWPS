package ec.edu.uteq.presustentaciones.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estado_anteproyecto")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EstadoAnteproyectoCatalogo {
    @Id
    @Column(name = "codigo", length = 30)
    private String codigo;
}
