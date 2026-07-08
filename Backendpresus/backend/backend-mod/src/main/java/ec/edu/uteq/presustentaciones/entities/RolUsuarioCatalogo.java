package ec.edu.uteq.presustentaciones.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rol_usuario")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RolUsuarioCatalogo {
    @Id
    @Column(name = "codigo", length = 20)
    private String codigo;

    @Column(name = "descripcion", length = 100)
    private String descripcion;
}
