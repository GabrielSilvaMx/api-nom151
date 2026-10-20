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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PscErrorResponseDto> handleValidationException(MethodArgumentNotValidException ex) {
        String firstError = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        PscErrorResponseDto error = new PscErrorResponseDto(
                false,
                true,
                firstError,
                LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}