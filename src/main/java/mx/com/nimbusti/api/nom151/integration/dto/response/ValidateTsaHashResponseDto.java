package mx.com.nimbusti.api.nom151.integration.dto.response;

/**
 * Represents the response returned after validating a TSA hash.
 *
 * <p>Contains the result of the validation and a descriptive message for the caller.</p>
 *
 * @param status {@code true} when the TSA hash validation succeeds; otherwise {@code false}.
 * @param message Human-readable description of the validation result.
 */
public record ValidateTsaHashResponseDto(
    Boolean status,
    String message
) {}