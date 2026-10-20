package mx.com.nimbusti.api.nom151.integration.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ValidateTsaHashRequestDto(
    @NotBlank(message = "El sello en Base64 es obligatorio")
    String fileTsa64,
    @NotBlank(message = "El hash SHA-256 original es obligatorio")
    String tsa_hash,
    @NotBlank(message = "El UUID de la transacción es obligatorio")
    String uuid
) {}