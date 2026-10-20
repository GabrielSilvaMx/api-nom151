package mx.com.nimbusti.api.nom151.integration.util;

import mx.com.nimbusti.api.nom151.integration.exception.PscIntegrationException;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.tsp.TimeStampTokenInfo;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Collection;

@Slf4j
@Component
public class PscCryptoValidator {

    public boolean validateTimeStampToken(String base64Tst, String expectedSha256Hash) {
        try {
            byte[] derTokenBytes = Base64.getDecoder().decode(base64Tst.trim());
            TimeStampToken timeStampToken = new TimeStampToken(
                    new org.bouncycastle.cms.CMSSignedData(derTokenBytes)
            );

            TimeStampTokenInfo info = timeStampToken.getTimeStampInfo();

            // 1. Validar Algoritmo de Digest (debe ser SHA-256)
            ASN1ObjectIdentifier hashAlgorithmOid = info.getHashAlgorithm().getAlgorithm();
            if (!NISTObjectIdentifiers.id_sha256.equals(hashAlgorithmOid)) {
                log.warn("El sello de tiempo no utiliza el algoritmo SHA-256. OID recibido: {}", hashAlgorithmOid);
                return false;
            }

            // 2. Compara Hash esperado vs embebido en ASN.1
            byte[] embeddedHashBytes = info.getMessageImprintDigest();
            String embeddedHashHex = Hex.toHexString(embeddedHashBytes);

            if (!embeddedHashHex.equalsIgnoreCase(expectedSha256Hash)) {
                log.error("El hash embebido en el sello ({}) no coincide con el hash esperado ({})",
                        embeddedHashHex, expectedSha256Hash);
                return false;
            }

            // 3. Verificar Firma Digital del PSC
            return verifyTokenSignature(timeStampToken);

        } catch (Exception e) {
            log.error("Error al desempaquetar o validar el TimeStampToken ASN.1/DER: {}", e.getMessage(), e);
            throw new PscIntegrationException("Error criptográfico al validar la estructura ASN.1 del Sello de Tiempo", e);
        }
    }

    public boolean validateNom151Certificate(String base64NomConstancy, String expectedSha256Hash) {
        return validateTimeStampToken(base64NomConstancy, expectedSha256Hash);
    }

    public boolean isValidDerStructure(byte[] derBytes) {
        try (ASN1InputStream asn1In = new ASN1InputStream(new ByteArrayInputStream(derBytes))) {
            ASN1Primitive obj = asn1In.readObject();
            return obj != null;
        } catch (Exception e) {
            log.warn("Formato DER/ASN.1 no válido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Corregido: Obtiene el SignerInformation desde SignerInformationStore y resuelve 
     * el certificado emisor a través de la coincidencia con el SID del firmante.
     */
    private boolean verifyTokenSignature(TimeStampToken timeStampToken) {
        try {
            // Extraer colecciones de certs y firmantes
            Store<X509CertificateHolder> certs = timeStampToken.getCertificates();
            SignerInformationStore signers = timeStampToken.toCMSSignedData().getSignerInfos();
            Collection<SignerInformation> c = signers.getSigners();

            if (c.isEmpty()) {
                log.warn("No se encontraron firmantes dentro del TimeStampToken");
                return false;
            }

            SignerInformation signer = c.iterator().next();
            Collection<X509CertificateHolder> matches = certs.getMatches(signer.getSID());

            if (matches.isEmpty()) {
                log.warn("No se encontró el certificado correspondiente al firmante en el token");
                return false;
            }

            X509CertificateHolder certHolder = matches.iterator().next();
            SignerInformationVerifier verifier = new JcaSimpleSignerInfoVerifierBuilder()
                    .setProvider(org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME)
                    .build(certHolder);

            timeStampToken.validate(verifier);
            log.info("Firma criptográfica del PSC validada correctamente para el certificado: {}", certHolder.getSubject());
            return true;

        } catch (Exception e) {
            log.error("Fallo al comprobar la firma del PSC en el sello: {}", e.getMessage());
            return false;
        }
    }
}