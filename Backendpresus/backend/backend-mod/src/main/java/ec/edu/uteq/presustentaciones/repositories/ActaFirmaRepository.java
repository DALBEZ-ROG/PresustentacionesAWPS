package ec.edu.uteq.presustentaciones.repositories;

import ec.edu.uteq.presustentaciones.entities.ActaFirma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActaFirmaRepository extends JpaRepository<ActaFirma, Long> {
    List<ActaFirma> findByActaId(Long actaId);
    Optional<ActaFirma> findByActaIdAndRolFirmante(Long actaId, String rolFirmante);
}
