package mx.com.nimbusti.api.nom151.integration.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "tsa_evidences",
    indexes = {
        @Index(name = "idx_tsa_uuid", columnList = "uuid", unique = true),
        @Index(name = "idx_tsa_hash", columnList = "tsa_hash")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TsaEvidence extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Folio identificador de la transacción retornado por PSC CODEX[cite: 2]
     */
    @Column(name = "uuid", nullable = false, length = 36, unique = true, updatable = false)
    private String uuid;

    /**
     * Hash SHA-256 en formato hexadecimal (64 caracteres)
     */
    @Column(name = "tsa_hash", nullable = false, length = 64, updatable = false)
    private String tsaHash;

    /**
     * Token TimeStamp (file) codificado en Base64
     */
    @Lob
    @Column(name = "file_tsa_base64", nullable = false, columnDefinition = "TEXT")
    private String fileTsaBase64;

    /**
     * Estado de la emisión retornado por el PSC[cite: 2]
     */
    @Column(name = "status", nullable = false)
    private Boolean status;
}