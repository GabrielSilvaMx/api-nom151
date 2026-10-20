package mx.com.nimbusti.api.nom151.integration.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TsaHashResponseDto(
    Boolean status,
    @JsonProperty("hash-processed") String hashProcessed,
    String file,
    String uuid
) {}