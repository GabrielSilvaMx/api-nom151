package mx.com.nimbusti.api.nom151.integration.repository;

import mx.com.nimbusti.api.nom151.integration.entity.TsaEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TsaEvidenceRepository extends JpaRepository<TsaEvidence, Long> {

    /**
     * Consulta por UUID asignado por PSC CODEX[cite: 2]
     */
    Optional<TsaEvidence> findByUuid(String uuid);

    /**
     * Consulta evidencias emitidas para un hash específico[cite: 2]
     */
    Optional<TsaEvidence> findByTsaHash(String tsaHash);

    boolean existsByTsaHash(String tsaHash);
}