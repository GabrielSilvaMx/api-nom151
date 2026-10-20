package mx.com.nimbusti.api.nom151.integration.service;

import mx.com.nimbusti.api.nom151.integration.client.PscCodexClient;
import mx.com.nimbusti.api.nom151.integration.dto.request.TsaHashRequestDto;
import mx.com.nimbusti.api.nom151.integration.dto.request.ValidateTsaHashRequestDto;
import mx.com.nimbusti.api.nom151.integration.dto.response.TsaHashResponseDto;
import mx.com.nimbusti.api.nom151.integration.dto.response.ValidateTsaHashResponseDto;
import mx.com.nimbusti.api.nom151.integration.entity.PscAuditLog;
import mx.com.nimbusti.api.nom151.integration.entity.TsaEvidence;
import mx.com.nimbusti.api.nom151.integration.exception.PscIntegrationException;
import mx.com.nimbusti.api.nom151.integration.repository.PscAuditLogRepository;
import mx.com.nimbusti.api.nom151.integration.repository.TsaEvidenceRepository;
import mx.com.nimbusti.api.nom151.integration.service.impl.TimeStampServiceImpl;
import mx.com.nimbusti.api.nom151.integration.util.PscCryptoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeStampServiceImplTest {

    @Mock
    private PscCodexClient pscCodexClient;

    @Mock
    private TsaEvidenceRepository tsaEvidenceRepository;

    @Mock
    private PscAuditLogRepository auditLogRepository;

    @Mock
    private PscCryptoValidator pscCryptoValidator;

    @InjectMocks
    private TimeStampServiceImpl timeStampService;

    private String validSha256Hash;
    private String sampleUuid;
    private String sampleBase64Tst;

    @BeforeEach
    void setUp() {
        validSha256Hash = "741e4284fc27be60854ab665f34b4345c1ae38e4822d056b7521f684661e3a6a";[cite: 1, 3]
        sampleUuid = "6e05ccc4-5b79-479c-9d89-774a05a4f0e1";[cite: 1, 3]
        sampleBase64Tst = "MIIUczADAgEAMIIUagYJKoZIhvcNAQcCoIIUWzCCFFcCAQMxDzANBglghkg...";[cite: 1, 3]
    }

    @Test
    @DisplayName("Debe emitir un Sello de Tiempo exitosamente a partir de un Hash SHA-256")
    void shouldCreateTimeStampFromHashSuccessfully() {
        // Arrange
        TsaHashRequestDto request = new TsaHashRequestDto(validSha256Hash);
        TsaHashResponseDto mockResponse = new TsaHashResponseDto(true, validSha256Hash, sampleBase64Tst, sampleUuid);

        when(pscCodexClient.requestTimeStamp(any(TsaHashRequestDto.class))).thenReturn(mockResponse);
        when(pscCryptoValidator.validateTimeStampToken(sampleBase64Tst, validSha256Hash)).thenReturn(true);
        when(tsaEvidenceRepository.save(any(TsaEvidence.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TsaHashResponseDto result = timeStampService.createTimeStampFromHash(request);

        // Assert
        assertNotNull(result);
        assertTrue(result.status());
        assertEquals(sampleUuid, result.uuid());
        assertEquals(validSha256Hash, result.hashProcessed());

        verify(pscCodexClient, times(1)).requestTimeStamp(request);
        verify(pscCryptoValidator, times(1)).validateTimeStampToken(sampleBase64Tst, validSha256Hash);
        verify(tsaEvidenceRepository, times(1)).save(any(TsaEvidence.class));
        verify(auditLogRepository, times(1)).save(any(PscAuditLog.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el Hash SHA-256 no tiene formato válido de 64 caracteres hex")
    void shouldThrowExceptionWhenHashIsInvalid() {
        // Arrange
        TsaHashRequestDto invalidRequest = new TsaHashRequestDto("invalid-hash");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> timeStampService.createTimeStampFromHash(invalidRequest)
        );

        assertTrue(exception.getMessage().contains("SHA-256"));
        verifyNoInteractions(pscCodexClient, tsaEvidenceRepository);
    }

    @Test
    @DisplayName("Debe lanzar PscIntegrationException si la validación criptográfica local del token ASN.1 falla")
    void shouldThrowExceptionWhenCryptoValidationFails() {
        // Arrange
        TsaHashRequestDto request = new TsaHashRequestDto(validSha256Hash);
        TsaHashResponseDto mockResponse = new TsaHashResponseDto(true, validSha256Hash, sampleBase64Tst, sampleUuid);

        when(pscCodexClient.requestTimeStamp(any(TsaHashRequestDto.class))).thenReturn(mockResponse);
        when(pscCryptoValidator.validateTimeStampToken(sampleBase64Tst, validSha256Hash)).thenReturn(false);

        // Act & Assert
        PscIntegrationException exception = assertThrows(
                PscIntegrationException.class,
                () -> timeStampService.createTimeStampFromHash(request)
        );

        assertTrue(exception.getMessage().contains("validación de integridad local"));
        verify(tsaEvidenceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe procesar un documento en MultipartFile, calcular su hash e inyectar el sello")
    void shouldCreateTimeStampFromMultipartFile() {
        // Arrange
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "contrato.pdf",
                "application/pdf",
                "Contenido binario de prueba".getBytes()
        );

        TsaHashResponseDto mockResponse = new TsaHashResponseDto(true, validSha256Hash, sampleBase64Tst, sampleUuid);

        when(pscCodexClient.requestTimeStamp(any(TsaHashRequestDto.class))).thenReturn(mockResponse);
        when(pscCryptoValidator.validateTimeStampToken(eq(sampleBase64Tst), anyString())).thenReturn(true);

        // Act
        TsaHashResponseDto result = timeStampService.createTimeStampFromDocument(mockFile);

        // Assert
        assertNotNull(result);
        verify(pscCodexClient, times(1)).requestTimeStamp(any(TsaHashRequestDto.class));
        verify(tsaEvidenceRepository, times(1)).save(any(TsaEvidence.class));
    }

    @Test
    @DisplayName("Debe validar un Sello de Tiempo existente llamando al cliente si pasa la verificación local")
    void shouldValidateTimeStampSuccessfully() {
        // Arrange
        ValidateTsaHashRequestDto validateDto = new ValidateTsaHashRequestDto(sampleBase64Tst, validSha256Hash, sampleUuid);
        ValidateTsaHashResponseDto mockPscValidation = new ValidateTsaHashResponseDto(true, "Sello válido");

        when(pscCryptoValidator.validateTimeStampToken(sampleBase64Tst, validSha256Hash)).thenReturn(true);
        when(pscCodexClient.validateTimeStamp(validateDto)).thenReturn(mockPscValidation);

        // Act
        ValidateTsaHashResponseDto response = timeStampService.validateTimeStamp(validateDto);

        // Assert
        assertNotNull(response);
        assertTrue(response.status());
        verify(auditLogRepository, times(1)).save(any(PscAuditLog.class));
    }
}