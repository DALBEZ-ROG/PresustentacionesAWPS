package ec.edu.uteq.presustentaciones.config;

import ec.edu.uteq.presustentaciones.entities.Docente;
import ec.edu.uteq.presustentaciones.entities.Estudiante;
import ec.edu.uteq.presustentaciones.entities.Usuario;
import ec.edu.uteq.presustentaciones.repositories.DocenteRepository;
import ec.edu.uteq.presustentaciones.repositories.EstudianteRepository;
import ec.edu.uteq.presustentaciones.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final DocenteRepository docenteRepository;
    private final EstudianteRepository estudianteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Solo insertar si no hay usuarios (BD vacía)
        if (usuarioRepository.count() > 0) {
            log.info("BD ya tiene usuarios, omitiendo inicialización de datos.");
            return;
        }

        log.info("Inicializando usuarios de prueba...");
        String password = passwordEncoder.encode("admin123");

        // Admin
        Usuario admin = crearUsuario("Coordinador", "Sistema", "coordinador@uteq.edu.ec", password, "ADMIN");

        // Docentes
        Usuario doc1 = crearUsuario("Roberto", "Martínez", "rmartinez@uteq.edu.ec", password, "DOCENTE");
        Usuario doc2 = crearUsuario("Diana", "Benítez", "dbenitez@uteq.edu.ec", password, "DOCENTE");
        Usuario doc3 = crearUsuario("Mario", "Castro", "mcastro@uteq.edu.ec", password, "DOCENTE");
        Usuario doc4 = crearUsuario("Andrea", "Rodríguez", "arodriguez@uteq.edu.ec", password, "DOCENTE");

        crearDocente(doc1, "Ingeniería de Software");
        crearDocente(doc2, "Base de Datos");
        crearDocente(doc3, "Redes y Seguridad");
        crearDocente(doc4, "Inteligencia Artificial");

        // Estudiantes
        Usuario est1 = crearUsuario("Juan", "Perez Lopez", "jperez@uteq.edu.ec", password, "ESTUDIANTE");
        Usuario est2 = crearUsuario("María", "García Vega", "mgarcia@uteq.edu.ec", password, "ESTUDIANTE");
        Usuario est3 = crearUsuario("Carlos", "López Mora", "clopez@uteq.edu.ec", password, "ESTUDIANTE");

        crearEstudiante(est1, "Ingeniería en Software", "10mo");
        crearEstudiante(est2, "Ingeniería en Software", "10mo");
        crearEstudiante(est3, "Ingeniería en Software", "9no");

        log.info("Usuarios de prueba creados exitosamente. Password para todos: admin123");
    }

    private Usuario crearUsuario(String nombre, String apellido, String email, String password, String rol) {
        Usuario u = Usuario.builder()
                .nombre(nombre).apellido(apellido)
                .email(email).password(password)
                .rol(rol).activo(true)
                .build();
        return usuarioRepository.save(u);
    }

    private void crearDocente(Usuario usuario, String area) {
        Docente d = Docente.builder()
                .usuario(usuario)
                .areaEspecialidad(area)
                .cargaHorariaSemanal(20)
                .disponible(true)
                .creadoEn(LocalDateTime.now())
                .build();
        docenteRepository.save(d);
    }

    private void crearEstudiante(Usuario usuario, String carrera, String semestre) {
        Estudiante e = Estudiante.builder()
                .usuario(usuario)
                .carrera(carrera)
                .semestre(semestre)
                .creadoEn(LocalDateTime.now())
                .build();
        estudianteRepository.save(e);
    }
}
