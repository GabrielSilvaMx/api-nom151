package mx.com.nimbusti.api.nom151.integration.repository;

import mx.com.nimbusti.api.nom151.integration.entity.PscAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PscAuditLogRepository extends JpaRepository<PscAuditLog, Long> {

    List<PscAuditLog> findByEvidenceUuidOrderByTimestampDesc(String evidenceUuid);
}