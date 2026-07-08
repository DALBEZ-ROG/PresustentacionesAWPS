package ec.edu.uteq.presustentaciones.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_mensaje")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TipoMensajeCatalogo {
    @Id
    @Column(name = "codigo", length = 30)
    private String codigo;
}
