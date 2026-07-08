package ec.edu.uteq.presustentaciones.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estado_solicitud")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EstadoSolicitudCatalogo {
    @Id
    @Column(name = "codigo", length = 30)
    private String codigo;
}
