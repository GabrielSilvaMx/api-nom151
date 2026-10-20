package mx.com.nimbusti.api.nom151.integration.service;

import mx.com.nimbusti.api.nom151.integration.dto.request.*;
import mx.com.nimbusti.api.nom151.integration.dto.response.*;
import org.springframework.web.multipart.MultipartFile;

public interface TimeStampService {
    /**
     * Genera el hash SHA-256 del documento binario y solicita el Sello Digital de Tiempo a PSC CODEX.
     */
    TsaHashResponseDto createTimeStampFromDocument(MultipartFile document);

    /**
     * Solicita el sello enviando directamente el hash SHA-256 precalculado.
     */
    TsaHashResponseDto createTimeStampFromHash(TsaHashRequestDto request);

    /**
     * Recupera un sello previamente emitido utilizando el UUID.
     */
    GetTsaHashResponseDto retrieveTimeStamp(String uuid);

    /**
     * Valida la autenticidad de un Sello de Tiempo.
     */
    ValidateTsaHashResponseDto validateTimeStamp(ValidateTsaHashRequestDto request);
}