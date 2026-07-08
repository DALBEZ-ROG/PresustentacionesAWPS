package ec.edu.uteq.presustentaciones.repositories;

import ec.edu.uteq.presustentaciones.entities.Modalidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModalidadRepository extends JpaRepository<Modalidad, String> {
}
