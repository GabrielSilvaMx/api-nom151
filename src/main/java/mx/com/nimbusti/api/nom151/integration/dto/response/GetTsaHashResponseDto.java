package mx.com.nimbusti.api.nom151.integration.dto.response;

public record GetTsaHashResponseDto(
    TsaResponse response
) {
    public record TsaResponse(
        Boolean status,
        String file
    ) {}
}