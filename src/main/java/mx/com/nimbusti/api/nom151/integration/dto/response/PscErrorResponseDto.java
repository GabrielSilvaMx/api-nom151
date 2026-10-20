package mx.com.nimbusti.api.nom151.integration.dto.response;

import java.time.LocalDateTime;

public record PscErrorResponseDto(
    Boolean status,
    Boolean error,
    String message,
    LocalDateTime timestamp
) {}