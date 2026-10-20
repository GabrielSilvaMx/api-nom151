package mx.com.nimbusti.api.nom151.integration.exception;

import mx.com.nimbusti.api.nom151.integration.dto.response.PscErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura errores de integración con PSC CODEX o fallos criptográficos locales.
     */
    @ExceptionHandler(PscIntegrationException.class)
    public ResponseEntity<PscErrorResponseDto> handlePscIntegrationException(PscIntegrationException ex) {
        PscErrorResponseDto error = new PscErrorResponseDto(
                false,
                true,
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_GATEWAY);
    }

    /**
     * Captura argumentos no válidos en la sintaxis de las peticiones (e.g. Hashes que no cumplen SHA-256).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<PscErrorResponseDto> handleIllegalArgumentException(IllegalArgumentException ex) {
        PscErrorResponseDto error = new PscErrorResponseDto(
                false,
                true,
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Captura fallos de validación producidos por anotaciones Jakarta Validation (@Valid / @NotBlank / @Pattern).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PscErrorResponseDto> handleValidationException(MethodArgumentNotValidException ex) {
        String firstError = ex.getBindingResult().getAllErrors().isEmpty() 
                ? "Solicitud no válida" 
                : ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        PscErrorResponseDto error = new PscErrorResponseDto(
                false,
                true,
                firstError,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Captura cualquier otra excepción no controlada en la JVM.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<PscErrorResponseDto> handleGenericException(Exception ex) {
        PscErrorResponseDto error = new PscErrorResponseDto(
                false,
                true,
                "Ocurrió un error interno procesando la solicitud: " + ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}