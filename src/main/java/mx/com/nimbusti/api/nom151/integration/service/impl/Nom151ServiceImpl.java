package mx.com.nimbusti.api.nom151.integration.service.impl;

import mx.com.nimbusti.api.nom151.integration.client.PscCodexClient;
import mx.com.nimbusti.api.nom151.integration.dto.request.NomHashRequestDto;
import mx.com.nimbusti.api.nom151.integration.dto.request.ValidateNomHashRequestDto;
import mx.com.nimbusti.api.nom151.integration.dto.response.NomHashResponseDto;
import mx.com.nimbusti.api.nom151.integration.dto.response.ValidateTsaHashResponseDto;
import mx.com.nimbusti.api.nom151.integration.entity.Nom151Evidence;
import mx.com.nimbusti.api.nom151.integration.entity.PscAuditLog;
import mx.com.nimbusti.api.nom151.integration.exception.PscIntegrationException;
import mx.com.nimbusti.api.nom151.integration.repository.Nom151EvidenceRepository;
import mx.com.nimbusti.api.nom151.integration.repository.PscAuditLogRepository;
import mx.com.nimbusti.api.nom151.integration.service.Nom151Service;
import mx.com.nimbusti.api.nom151.integration.util.HashUtils;
import mx.com.nimbusti.api.nom151.integration.util.PscCryptoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class Nom151ServiceImpl implements Nom151Service {

    private final PscCodexClient pscCodexClient;
    private final Nom151EvidenceRepository nom151EvidenceRepository;
    private final PscAuditLogRepository auditLogRepository;
    private final PscCryptoValidator pscCryptoValidator;

    @Override
    @Transactional
    public NomHashResponseDto requestCertificate(NomHashRequestDto request) {
        String nomHash = request.nom_hash();
        log.info("Solicitando Constancia NOM-151 para Hash SHA-256: {}", nomHash);

        // 1. Validar sintaxis del Hash SHA-256
        if (!HashUtils.isValidSha256Hex(nomHash)) {
            throw new IllegalArgumentException("El nom_hash no cumple con el formato SHA-256 de 64 caracteres hex");
        }

        // 2. Invocar servicio REST de PSC CODEX
        NomHashResponseDto pscResponse = pscCodexClient.requestNom151(request);

        if (pscResponse == null || Boolean.FALSE.equals(pscResponse.status())) {
            log.error("Fallo la emisión de la Constancia NOM-151 para hash: {}", nomHash);
            throw new PscIntegrationException("Error en la respuesta de PSC CODEX al solicitar Constancia NOM-151");
        }

        // 3. Validación Criptográfica ASN.1 de la Constancia recibida
        boolean isValid = pscCryptoValidator.validateNom151Certificate(pscResponse.file(), nomHash);
        if (!isValid) {
            log.error("La constancia NOM-151 retornada no pasó la validación local para el UUID: {}", pscResponse.uuid());
            throw new PscIntegrationException("La constancia NOM-151 no superó la verificación criptográfica de la firma del PSC");
        }

        // 4. Persistencia Inalterable en la Base de Datos
        Nom151Evidence evidence = Nom151Evidence.builder()
                .uuid(pscResponse.uuid())
                .nomHash(nomHash)
                .fileNomBase64(pscResponse.file())
                .status(pscResponse.status())
                .build();
        nom151EvidenceRepository.save(evidence);

        // 5. Registro en Bitácora de Auditoría
        registerAuditLog(PscAuditLog.TransactionType.NOM151, pscResponse.uuid(), 
                PscAuditLog.AuditAction.REQUESTED, "Constancia NOM-151 emitida y validada exitosamente");

        return pscResponse;
    }

    @Override
    @Transactional
    public ValidateTsaHashResponseDto validateCertificate(ValidateNomHashRequestDto request) {
        log.info("Validando Constancia NOM-151 para UUID: {}", request.uuid());

        // 1. Verificar firma DER/ASN.1 localmente
        boolean localValid = pscCryptoValidator.validateNom151Certificate(request.fileNom64(), request.nom_hash());

        if (!localValid) {
            registerAuditLog(PscAuditLog.TransactionType.NOM151, request.uuid(), 
                    PscAuditLog.AuditAction.VALIDATION_FAILED, "Fallo la validación local de la constancia NOM-151");
            return new ValidateTsaHashResponseDto(false, "La Constancia NOM-151 es inválida o no corresponde al mensaje de datos");
        }

        // 2. Confirmación con endpoint de PSC CODEX
        ValidateTsaHashResponseDto pscValidation = pscCodexClient.validateNom151(request);

        PscAuditLog.AuditAction action = Boolean.TRUE.equals(pscValidation.status()) ? 
                PscAuditLog.AuditAction.VALIDATED : PscAuditLog.AuditAction.VALIDATION_FAILED;

        registerAuditLog(PscAuditLog.TransactionType.NOM151, request.uuid(), action, pscValidation.message());

        return pscValidation;
    }

    private void registerAuditLog(PscAuditLog.TransactionType type, String uuid, PscAuditLog.AuditAction action, String message) {
        PscAuditLog auditLog = PscAuditLog.builder()
                .transactionType(type)
                .evidenceUuid(uuid)
                .action(action)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(auditLog);
    }
}