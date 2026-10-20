package mx.com.nimbusti.api.nom151.integration.repository;

import mx.com.nimbusti.api.nom151.integration.entity.Nom151Evidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Nom151EvidenceRepository extends JpaRepository<Nom151Evidence, Long> {

    /**
     * Recuperación de Constancia por UUID[cite: 2]
     */
    Optional<Nom151Evidence> findByUuid(String uuid);

    /**
     * Búsqueda por el nom_hash procesado[cite: 2]
     */
    Optional<Nom151Evidence> findByNomHash(String nomHash);

    boolean existsByNomHash(String nomHash);
}