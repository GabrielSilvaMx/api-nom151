package mx.com.nimbusti.api.nom151.integration.client.impl;

import mx.com.nimbusti.api.nom151.integration.client.PscCodexClient;
import mx.com.nimbusti.api.nom151.integration.dto.request.*;
import mx.com.nimbusti.api.nom151.integration.dto.response.*;
import mx.com.nimbusti.api.nom151.integration.exception.PscIntegrationException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class PscCodexClientImpl implements PscCodexClient {

    private final RestClient pscRestClient;

    @Override
    @Retry(name = "pscClientRetry")
    @CircuitBreaker(name = "pscClientCircuitBreaker", fallbackMethod = "requestTimeStampFallback")
    public TsaHashResponseDto requestTimeStamp(TsaHashRequestDto request) {
        log.info("Solicitando Sello Digital de Tiempo para hash: {}", request.tsa_hash()); //[cite: 1]

        return pscRestClient.post()
                .uri("/api/certificate/tsa-hash") //[cite: 1, 2]
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    log.error("Error devuelto por PSC CODEX TSA. Código HTTP: {}", resp.getStatusCode());
                    throw new PscIntegrationException("Error al solicitar el Sello de Tiempo al PSC"); //[cite: 1, 2]
                })
                .body(TsaHashResponseDto.class);
    }

    @Override
    @Retry(name = "pscClientRetry")
    @CircuitBreaker(name = "pscClientCircuitBreaker")
    public GetTsaHashResponseDto getTimeStampByUuid(GetTsaHashRequestDto request) {
        log.info("Recuperando Sello de Tiempo por UUID: {}", request.uuid()); //[cite: 1, 2]

        return pscRestClient.post()
                .uri("/api/certificate/get-tsa-hash") //[cite: 1, 2]
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    throw new PscIntegrationException("Error al consultar Sello de Tiempo por UUID"); //[cite: 1, 2]
                })
                .body(GetTsaHashResponseDto.class);
    }

    @Override
    @Retry(name = "pscClientRetry")
    @CircuitBreaker(name = "pscClientCircuitBreaker")
    public ValidateTsaHashResponseDto validateTimeStamp(ValidateTsaHashRequestDto request) {
        log.info("Validando Sello de Tiempo para UUID: {}", request.uuid()); //[cite: 1, 2]

        return pscRestClient.post()
                .uri("/api/certificate/validate-tsa-hash") //[cite: 1, 2]
                .body(request)
                .retrieve()
                .body(ValidateTsaHashResponseDto.class);
    }

    @Override
    @Retry(name = "pscClientRetry")
    @CircuitBreaker(name = "pscClientCircuitBreaker")
    public NomHashResponseDto requestNom151(NomHashRequestDto request) {
        log.info("Solicitando Constancia NOM-151 para hash: {}", request.nom_hash()); //[cite: 2]

        return pscRestClient.post()
                .uri("/api/certificate/nom-hash") //[cite: 2]
                .body(request)
                .retrieve()
                .body(NomHashResponseDto.class);
    }

    @Override
    @Retry(name = "pscClientRetry")
    @CircuitBreaker(name = "pscClientCircuitBreaker")
    public ValidateTsaHashResponseDto validateNom151(ValidateNomHashRequestDto request) {
        log.info("Validando Constancia NOM-151 para UUID: {}", request.uuid()); //[cite: 2]

        return pscRestClient.post()
                .uri("/api/certificate/validate-nom-hash") //[cite: 2]
                .body(request)
                .retrieve()
                .body(ValidateTsaHashResponseDto.class);
    }

    // --- Métodos Fallback para Manejo de Fallas de Red ---
    public TsaHashResponseDto requestTimeStampFallback(TsaHashRequestDto request, Throwable t) {
        log.error("Fallback activado para Sello de Tiempo debido a: {}", t.getMessage());
        throw new PscIntegrationException("El servicio de Sellado de Tiempo no se encuentra disponible temporalmente. Reintente más tarde.");
    }
}