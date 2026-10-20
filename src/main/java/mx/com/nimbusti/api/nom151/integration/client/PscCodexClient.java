package mx.com.nimbusti.api.nom151.integration.client;

import mx.com.nimbusti.api.nom151.integration.dto.request.*;
import mx.com.nimbusti.api.nom151.integration.dto.response.*;

public interface PscCodexClient {
    TsaHashResponseDto requestTimeStamp(TsaHashRequestDto request);
    GetTsaHashResponseDto getTimeStampByUuid(GetTsaHashRequestDto request);
    ValidateTsaHashResponseDto validateTimeStamp(ValidateTsaHashRequestDto request);
    
    // Métodos NOM-151
    NomHashResponseDto requestNom151(NomHashRequestDto request);
    ValidateTsaHashResponseDto validateNom151(ValidateNomHashRequestDto request);
}