package mx.com.nimbusti.api.nom151.integration.service;

import mx.com.nimbusti.api.nom151.integration.client.PscCodexClient;
import mx.com.nimbusti.api.nom151.integration.dto.request.NomHashRequestDto;
import mx.com.nimbusti.api.nom151.integration.dto.response.NomHashResponseDto;
import mx.com.nimbusti.api.nom151.integration.entity.Nom151Evidence;
import mx.com.nimbusti.api.nom151.integration.entity.PscAuditLog;
import mx.com.nimbusti.api.nom151.integration.exception.PscIntegrationException;
import mx.com.nimbusti.api.nom151.integration.repository.Nom151EvidenceRepository;
import mx.com.nimbusti.api.nom151.integration.repository.PscAuditLogRepository;
import mx.com.nimbusti.api.nom151.integration.service.impl.Nom151ServiceImpl;
import mx.com.nimbusti.api.nom151.integration.util.PscCryptoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Nom151ServiceImplTest {

    @Mock
    private PscCodexClient pscCodexClient;

    @Mock
    private Nom151EvidenceRepository nom151EvidenceRepository;

    @Mock
    private PscAuditLogRepository auditLogRepository;

    @Mock
    private PscCryptoValidator pscCryptoValidator;

    @InjectMocks
    private Nom151ServiceImpl nom151Service;

    private String validNomHash;
    private String sampleUuid;
    private String sampleBase64Nom;

    @BeforeEach
    void setUp() {
        validNomHash = "741e4284fc27be60854ab665f34b4345c1ae38e4822d056b7521f684661e3a6a";
        sampleUuid = "cbc3b8e0-69fb-4b24-b127-f448e112f020";
        sampleBase64Nom = "MIIUczADAgEAMIIUagYJKoZIhvcNAQcCoIIUWzCCFFcCAQMxDzANBglghkg";
    }

    @Test
    @DisplayName("Debe emitir y guardar una Constancia NOM-151 cuando el hash es válido")
    void shouldRequestNom151CertificateSuccessfully() {
        NomHashRequestDto request = new NomHashRequestDto(validNomHash);
        NomHashResponseDto mockResponse = new NomHashResponseDto(true, validNomHash, sampleBase64Nom, sampleUuid);

        when(pscCodexClient.requestNom151(request)).thenReturn(mockResponse);
        when(pscCryptoValidator.validateNom151Certificate(sampleBase64Nom, validNomHash)).thenReturn(true);

        NomHashResponseDto response = nom151Service.requestCertificate(request);

        assertNotNull(response);
        assertTrue(response.status());
        assertEquals(sampleUuid, response.uuid());

        verify(nom151EvidenceRepository, times(1)).save(any(Nom151Evidence.class));
        verify(auditLogRepository, times(1)).save(any(PscAuditLog.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si PSC CODEX devuelve un objeto nulo o fallido")
    void shouldThrowExceptionWhenPscResponseIsInvalid() {
        NomHashRequestDto request = new NomHashRequestDto(validNomHash);
        when(pscCodexClient.requestNom151(request)).thenReturn(null);

        assertThrows(PscIntegrationException.class, () -> nom151Service.requestCertificate(request));
        verify(nom151EvidenceRepository, never()).save(any());
    }
}