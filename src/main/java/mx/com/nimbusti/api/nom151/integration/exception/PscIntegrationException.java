package mx.com.nimbusti.api.nom151.integration.exception;

public class PscIntegrationException extends RuntimeException {

    public PscIntegrationException(String message) {
        super(message);
    }

    public PscIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}