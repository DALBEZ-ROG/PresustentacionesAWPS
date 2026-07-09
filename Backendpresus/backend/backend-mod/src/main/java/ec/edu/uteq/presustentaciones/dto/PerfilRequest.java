package ec.edu.uteq.presustentaciones.dto;

import lombok.Data;

@Data
public class PerfilRequest {
    private String nombre;
    private String apellido;
    private String email;
    private String emailNotificaciones;
    private String telefono;
    private String passwordActual;
    private String passwordNueva;
}