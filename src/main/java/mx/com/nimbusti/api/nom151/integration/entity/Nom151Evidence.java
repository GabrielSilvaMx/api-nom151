package mx.com.nimbusti.api.nom151.integration.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "nom151_evidences",
    indexes = {
        @Index(name = "idx_nom151_uuid", columnList = "uuid", unique = true),
        @Index(name = "idx_nom151_hash", columnList = "nom_hash")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Nom151Evidence extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Folio identificador de la transacción devuelto por el PSC[cite: 2]
     */
    @Column(name = "uuid", nullable = false, length = 36, unique = true, updatable = false)
    private String uuid;

    /**
     * Huella digital SHA-256 del mensaje de datos[cite: 2, 5]
     */
    @Column(name = "nom_hash", nullable = false, length = 64, updatable = false)
    private String nomHash;

    /**
     * Constancia de Conservación en formato Base64[cite: 2]
     */
    @Lob
    @Column(name = "file_nom_base64", nullable = false, columnDefinition = "TEXT")
    private String fileNomBase64;

    /**
     * Estado de la constancia[cite: 2]
     */
    @Column(name = "status", nullable = false)
    private Boolean status;
}