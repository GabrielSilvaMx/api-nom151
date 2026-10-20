package mx.com.nimbusti.api.nom151.integration.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "psc_audit_logs",
    indexes = {
        @Index(name = "idx_audit_evidence_uuid", columnList = "evidence_uuid")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PscAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_type", nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType; // TSA, NOM151

    @Column(name = "evidence_uuid", nullable = false, length = 36)
    private String evidenceUuid;

    @Column(name = "action", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AuditAction action; // REQUESTED, RETRIEVED, VALIDATED, VALIDATION_FAILED

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "message", length = 255)
    private String message;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public enum TransactionType { TSA, NOM151 }
    public enum AuditAction { REQUESTED, RETRIEVED, VALIDATED, VALIDATION_FAILED }
}