package ec.edu.uteq.presustentaciones.services;

import ec.edu.uteq.presustentaciones.dto.PerfilRequest;
import ec.edu.uteq.presustentaciones.entities.Usuario;
import ec.edu.uteq.presustentaciones.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UsuarioServiceImpl implements IUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Usuario crear(Usuario usuario) {
        log.info("Creando usuario con email: {}", usuario.getEmail());

        if (existePorEmail(usuario.getEmail())) {
            throw new RuntimeException("Ya existe un usuario con el email: " + usuario.getEmail());
        }

        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario actualizar(Long id, Usuario usuario) {
        log.info("Actualizando usuario con ID: {}", id);

        Usuario existente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        existente.setNombre(usuario.getNombre());
        existente.setApellido(usuario.getApellido());
        existente.setEmail(usuario.getEmail());
        existente.setRol(usuario.getRol());
        if (usuario.getTelefono() != null) {
            existente.setTelefono(usuario.getTelefono());
        }

        return usuarioRepository.save(existente);
    }

    @Override
    public void eliminar(Long id) {
        log.info("Eliminando usuario con ID: {}", id);

        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }

        usuarioRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    @Override
    public void activar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.setActivo(true);
        usuarioRepository.save(usuario);
    }

    @Override
    public void desactivar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public Usuario actualizarPerfil(Long id, String emailNotificaciones, String telefono) {
        int updated = usuarioRepository.actualizarPerfil(id, emailNotificaciones, telefono);
        if (updated == 0) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public Usuario actualizarPerfilCompleto(Long id, ec.edu.uteq.presustentaciones.dto.PerfilRequest req) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        if (req.getNombre() != null && !req.getNombre().isBlank()) {
            usuario.setNombre(req.getNombre().trim());
        }
        if (req.getApellido() != null && !req.getApellido().isBlank()) {
            usuario.setApellido(req.getApellido().trim());
        }
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            String nuevoEmail = req.getEmail().trim();
            // Validar que no exista otro usuario con ese email
            if (!nuevoEmail.equals(usuario.getEmail())) {
                if (usuarioRepository.existsByEmail(nuevoEmail)) {
                    throw new RuntimeException("El correo '" + nuevoEmail + "' ya esta registrado por otro usuario.");
                }
            }
            usuario.setEmail(nuevoEmail);
        }
        if (req.getEmailNotificaciones() != null) {
            usuario.setEmailNotificaciones(req.getEmailNotificaciones().trim());
        }
        if (req.getTelefono() != null) {
            usuario.setTelefono(req.getTelefono().trim());
        }

        // Cambio de contrasena
        if (req.getPasswordNueva() != null && !req.getPasswordNueva().isBlank()) {
            if (req.getPasswordActual() == null || req.getPasswordActual().isBlank()) {
                throw new RuntimeException("Debes ingresar tu contrasena actual para cambiarla.");
            }
            if (!passwordEncoder.matches(req.getPasswordActual(), usuario.getPassword())) {
                throw new RuntimeException("La contrasena actual es incorrecta.");
            }
            if (req.getPasswordNueva().length() < 4) {
                throw new RuntimeException("La nueva contrasena debe tener al menos 4 caracteres.");
            }
            usuario.setPassword(passwordEncoder.encode(req.getPasswordNueva()));
        }

        return usuarioRepository.save(usuario);
    }
}