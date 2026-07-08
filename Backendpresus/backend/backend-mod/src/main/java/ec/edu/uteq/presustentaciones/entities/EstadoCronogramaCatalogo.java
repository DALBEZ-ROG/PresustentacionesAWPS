package ec.edu.uteq.presustentaciones.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estado_cronograma")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EstadoCronogramaCatalogo {
    @Id
    @Column(name = "codigo", length = 30)
    private String codigo;
}
