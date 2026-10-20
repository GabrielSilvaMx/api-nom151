package mx.com.nimbusti.api.nom151.integration.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ValidateNomHashRequestDto(
    @NotBlank(message = "El archivo en Base64 es obligatorio")
    String fileNom64,
    @NotBlank(message = "El hash original es obligatorio")
    String nom_hash,
    @NotBlank(message = "El UUID es obligatorio")
    String uuid
) {}