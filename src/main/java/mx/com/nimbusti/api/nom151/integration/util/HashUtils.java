package mx.com.nimbusti.api.nom151.integration.util;

import mx.com.nimbusti.api.nom151.integration.exception.PscIntegrationException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtils {

    private static final String SHA_256_ALGORITHM = "SHA-256";

    private HashUtils() {
        // Constructor privado para evitar instanciación
    }

    /**
     * Calcula el digest SHA-256 de un arreglo de bytes y devuelve su representación Hexadecimal.
     */
    public static String calculateSha256Hex(byte[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("Los datos para generar el hash no pueden ser nulos o vacíos");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256_ALGORITHM);
            byte[] encodedHash = digest.digest(data);
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new PscIntegrationException("Error al obtener el algoritmo SHA-256", e);
        }
    }

    /**
     * Calcula el digest SHA-256 leyendo un archivo en streaming (óptimo para archivos grandes).
     */
    public static String calculateSha256Hex(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo proporcionado está vacío o es nulo");
        }
        try (InputStream inputStream = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance(SHA_256_ALGORITHM);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            return bytesToHex(digest.digest());
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new PscIntegrationException("Error al procesar el archivo para generar hash SHA-256", e);
        }
    }

    /**
     * Valida si una cadena de texto es un hash SHA-256 hexadecimal válido (64 caracteres hex).
     */
    public static boolean isValidSha256Hex(String hash) {
        return hash != null && hash.matches("^[a-fA-F0-9]{64}$");
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}