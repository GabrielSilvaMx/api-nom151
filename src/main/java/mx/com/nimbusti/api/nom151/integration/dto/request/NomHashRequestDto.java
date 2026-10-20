package mx.com.nimbusti.api.nom151.integration.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record NomHashRequestDto(
    @NotBlank(message = "El nom_hash es obligatorio")
    @Pattern(regexp = "^[a-fA-F0-9]{64}$", message = "El hash debe ser SHA-256 hexadecimal válido")
    String nom_hash
) {}