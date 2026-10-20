package mx.com.nimbusti.api.nom151.integration.util;

import mx.com.nimbusti.api.nom151.integration.exception.PscIntegrationException;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.SignerInformation;
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

    /**
     * Valida un TimeStampToken (RFC 3161) codificado en Base64 contra el hash SHA-256 esperado[cite: 1, 2].
     * 
     * @param base64Tst Token de sello de tiempo en Base64 (retornado por PSC CODEX)[cite: 1, 2]
     * @param expectedSha256Hash Hash SHA-256 hexadecimal esperado[cite: 1, 2]
     * @return true si la estructura es válida y el hash coincide
     */
    public boolean validateTimeStampToken(String base64Tst, String expectedSha256Hash) {
        try {
            byte[] derTokenBytes = Base64.getDecoder().decode(base64Tst.trim());
            TimeStampToken timeStampToken = new TimeStampToken(org.bouncycastle.cms.CMSSignedData.class.cast(
                    new org.bouncycastle.cms.CMSSignedData(derTokenBytes)));

            TimeStampTokenInfo info = timeStampToken.getTimeStampInfo();

            // 1. Validar Algoritmo de Digest (debe ser SHA-256)[cite: 1, 3]
            ASN1ObjectIdentifier hashAlgorithmOid = info.getHashAlgorithm().getAlgorithm();
            if (!NISTObjectIdentifiers.id_sha256.equals(hashAlgorithmOid)) {
                log.warn("El sello de tiempo no utiliza el algoritmo SHA-256. OID recibido: {}", hashAlgorithmOid);
                return false;
            }

            // 2. Extraer Hash Embebido en la estructura ASN.1 y comparar
            byte[] embeddedHashBytes = info.getMessageImprintDigest();
            String embeddedHashHex = Hex.toHexString(embeddedHashBytes);

            if (!embeddedHashHex.equalsIgnoreCase(expectedSha256Hash)) {
                log.error("El hash embebido en el sello ({}) no coincide con el hash esperado ({})",
                        embeddedHashHex, expectedSha256Hash);
                return false;
            }

            // 3. Verificar Firma Digital del PSC con el certificado emisor adjunto en el token[cite: 1]
            return verifyTokenSignature(timeStampToken);

        } catch (Exception e) {
            log.error("Error al desempaquetar o validar el TimeStampToken ASN.1/DER: {}", e.getMessage(), e);
            throw new PscIntegrationException("Error criptográfico al validar la estructura ASN.1 del Sello de Tiempo", e);
        }
    }

    /**
     * Valida una Constancia de Conservación NOM-151 codificada en Base64[cite: 1, 2].
     * 
     * @param base64NomConstancy Constancia NOM-151 en Base64[cite: 1, 2]
     * @param expectedSha256Hash Hash SHA-256 del mensaje de datos[cite: 1, 2]
     */
    public boolean validateNom151Certificate(String base64NomConstancy, String expectedSha256Hash) {
        // En la norma NOM-151, la constancia se emite como un bloque ASN.1 DER / CMS SignedData[cite: 1, 3].
        // Reutilizamos la verificación del sello/CMS sobre el hash del mensaje procesado[cite: 1, 2].
        return validateTimeStampToken(base64NomConstancy, expectedSha256Hash);
    }

    /**
     * Inspecciona la estructura ASN.1 en busca de anomalías en el encoding DER.
     */
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
     * Verifica la firma PKCS#7 / CMS dentro del TimeStampToken[cite: 1].
     */
    private boolean verifyTokenSignature(TimeStampToken timeStampToken) {
        try {
            Store<X509CertificateHolder> certs = timeStampToken.getCertificates();
            SignerInformation signer = timeStampToken.getSID() != null ? 
                    timeStampToken.getCertificates().getMatches(null).iterator().next() : null;

            Collection<X509CertificateHolder> matches = certs.getMatches(timeStampToken.getSID());
            if (matches.isEmpty()) {
                log.warn("No se encontró el certificado del PSC dentro del TimeStampToken");
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