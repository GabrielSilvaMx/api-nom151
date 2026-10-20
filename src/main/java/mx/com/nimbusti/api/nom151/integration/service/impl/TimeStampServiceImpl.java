package mx.com.nimbusti.api.nom151.integration.service.impl;

import mx.com.nimbusti.api.nom151.integration.client.PscCodexClient;
import mx.com.nimbusti.api.nom151.integration.dto.request.GetTsaHashRequestDto;
import mx.com.nimbusti.api.nom151.integration.dto.request.TsaHashRequestDto;
import mx.com.nimbusti.api.nom151.integration.dto.request.ValidateTsaHashRequestDto;
import mx.com.nimbusti.api.nom151.integration.dto.response.GetTsaHashResponseDto;
import mx.com.nimbusti.api.nom151.integration.dto.response.TsaHashResponseDto;
import mx.com.nimbusti.api.nom151.integration.dto.response.ValidateTsaHashResponseDto;
import mx.com.nimbusti.api.nom151.integration.entity.PscAuditLog;
import mx.com.nimbusti.api.nom151.integration.entity.TsaEvidence;
import mx.com.nimbusti.api.nom151.integration.exception.PscIntegrationException;
import mx.com.nimbusti.api.nom151.integration.repository.PscAuditLogRepository;
import mx.com.nimbusti.api.nom151.integration.repository.TsaEvidenceRepository;
import mx.com.nimbusti.api.nom151.integration.service.TimeStampService;
import mx.com.nimbusti.api.nom151.integration.util.HashUtils;
import mx.com.nimbusti.api.nom151.integration.util.PscCryptoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeStampServiceImpl implements TimeStampService {

    private final PscCodexClient pscCodexClient;
    private final TsaEvidenceRepository tsaEvidenceRepository;
    private final PscAuditLogRepository auditLogRepository;
    private final PscCryptoValidator pscCryptoValidator;

    @Override
    @Transactional
    public TsaHashResponseDto createTimeStampFromDocument(MultipartFile document) {
        log.info("Iniciando solicitud de Sello de Tiempo para documento: {}", document.getOriginalFilename());
        
        // 1. Calcular el Hash SHA-256 localmente (Privacidad: el documento no sale)
        String sha256Hex = HashUtils.calculateSha256Hex(document);
        return createTimeStampFromHash(new TsaHashRequestDto(sha256Hex));
    }

    @Override
    @Transactional
    public TsaHashResponseDto createTimeStampFromHash(TsaHashRequestDto request) {
        String hash = request.tsa_hash();
        log.info("Procesando solicitud de Sello de Tiempo para Hash SHA-256: {}", hash);

        // 1. Validar sintaxis del Hash SHA-256 (64 caracteres hexadecimales)[cite: 1, 2]
        if (!HashUtils.isValidSha256Hex(hash)) {
            throw new IllegalArgumentException("El hash enviado no cumple con el formato SHA-256 válido (64 caracteres hex)");[cite: 2]
        }

        // 2. Invocar al proveedor PSC CODEX vía REST client[cite: 1, 2]
        TsaHashResponseDto pscResponse = pscCodexClient.requestTimeStamp(request);

        if (pscResponse == null || Boolean.FALSE.equals(pscResponse.status())) {
            log.error("Fallo la emisión del sello en PSC CODEX para hash: {}", hash);
            throw new PscIntegrationException("Error en la respuesta de PSC CODEX al emitir Sello de Tiempo");[cite: 2]
        }

        // 3. Validación criptográfica local ASN.1 / Bouncy Castle del sello recibido[cite: 1]
        boolean isTokenValid = pscCryptoValidator.validateTimeStampToken(pscResponse.file(), hash);
        if (!isTokenValid) {
            log.error("La validación criptográfica local del sello falló para el UUID: {}", pscResponse.uuid());
            throw new PscIntegrationException("El sello retornado por PSC CODEX no pasó la validación de integridad local");
        }

        // 4. Persistir Evidencia Inalterable en la Base de Datos[cite: 1, 2]
        TsaEvidence evidence = TsaEvidence.builder()
                .uuid(pscResponse.uuid())
                .tsaHash(hash)
                .fileTsaBase64(pscResponse.file())
                .status(pscResponse.status())
                .build();
        tsaEvidenceRepository.save(evidence);

        // 5. Registrar en la Bitácora de Auditoría[cite: 1]
        registerAuditLog(PscAuditLog.TransactionType.TSA, pscResponse.uuid(), 
                PscAuditLog.AuditAction.REQUESTED, "Sello de Tiempo emitido y verificado con éxito");

        return pscResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public GetTsaHashResponseDto retrieveTimeStamp(String uuid) {
        log.info("Consultando Sello de Tiempo local o remoto para UUID: {}", uuid);

        // 1. Buscar primero en la base de datos local
        return tsaEvidenceRepository.findByUuid(uuid)
                .map(ev -> new GetTsaHashResponseDto(new GetTsaHashResponseDto.TsaResponse(ev.getStatus(), ev.getFileTsaBase64())))
                .orElseGet(() -> {
                    // 2. Si no está en BD local, consultar a PSC CODEX[cite: 1, 2]
                    GetTsaHashResponseDto remoteResp = pscCodexClient.getTimeStampByUuid(new GetTsaHashRequestDto(uuid));
                    registerAuditLog(PscAuditLog.TransactionType.TSA, uuid, 
                            PscAuditLog.AuditAction.RETRIEVED, "Sello de Tiempo recuperado desde PSC CODEX");
                    return remoteResp;
                });
    }

    @Override
    @Transactional
    public ValidateTsaHashResponseDto validateTimeStamp(ValidateTsaHashRequestDto request) {
        log.info("Iniciando validación de Sello de Tiempo para UUID: {}", request.uuid());

        // 1. Validación local criptográfica ASN.1 previa[cite: 1]
        boolean localValid = pscCryptoValidator.validateTimeStampToken(request.fileTsa64(), request.tsa_hash());
        
        if (!localValid) {
            registerAuditLog(PscAuditLog.TransactionType.TSA, request.uuid(), 
                    PscAuditLog.AuditAction.VALIDATION_FAILED, "Fallo de validación criptográfica local");
            return new ValidateTsaHashResponseDto(false, "El sello no corresponde al hash o su estructura ASN.1 es inválida");
        }

        // 2. Confirmación contra PSC CODEX[cite: 1, 2]
        ValidateTsaHashResponseDto pscValidation = pscCodexClient.validateTimeStamp(request);

        PscAuditLog.AuditAction action = Boolean.TRUE.equals(pscValidation.status()) ? 
                PscAuditLog.AuditAction.VALIDATED : PscAuditLog.AuditAction.VALIDATION_FAILED;
        
        registerAuditLog(PscAuditLog.TransactionType.TSA, request.uuid(), action, pscValidation.message());

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