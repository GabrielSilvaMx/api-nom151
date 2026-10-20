package mx.com.nimbusti.api.nom151.integration.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TsaHashRequestDto(
    @NotBlank(message = "El hash es obligatorio")
    @Pattern(regexp = "^[a-fA-F0-9]{64}$", message = "El hash debe ser un algoritmo SHA-256 en formato hexadecimal de 64 caracteres")
    String tsa_hash
) {}