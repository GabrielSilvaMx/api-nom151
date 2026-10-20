package mx.com.nimbusti.api.nom151.integration.service;

import mx.com.nimbusti.api.nom151.integration.dto.request.NomHashRequestDto;
import mx.com.nimbusti.api.nom151.integration.dto.request.ValidateNomHashRequestDto;
import mx.com.nimbusti.api.nom151.integration.dto.response.NomHashResponseDto;
import mx.com.nimbusti.api.nom151.integration.dto.response.ValidateTsaHashResponseDto;

public interface Nom151Service {
    /**
     * Solicita una Constancia de Conservación NOM-151 para un digest SHA-256 de un mensaje de datos.
     */
    NomHashResponseDto requestCertificate(NomHashRequestDto request);

    /**
     * Valida la autenticidad de una Constancia NOM-151 recibida.
     */
    ValidateTsaHashResponseDto validateCertificate(ValidateNomHashRequestDto request);
}