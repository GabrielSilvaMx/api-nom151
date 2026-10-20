package mx.com.nimbusti.api.nom151.integration.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GetTsaHashRequestDto(
    @NotBlank(message = "El UUID es obligatorio")
    String uuid
) {}