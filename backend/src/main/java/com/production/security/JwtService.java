package com.production.security;

import jakarta.inject.Singleton;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Service for generating and validating real, cryptographically signed JSON Web Tokens (JWT).
 * Built according to the RFC 7519 open standard using the HMAC-SHA256 (HS256) signature algorithm.
 * 
 * Anatomy of a JWT:
 * [Header Base64Url] . [Payload Base64Url] . [HMAC-SHA256 Signature Base64Url]
 */
@Singleton
public class JwtService {

    // 256-bit secret key used to sign and verify HMAC-SHA256 signatures
    private static final String SECRET_KEY = "ProductionTrackerEnterpriseSecretSigningKey2026";
    private static final long EXPIRATION_HOURS = 24; // Token validity period

    // Fixed JWT Header: {"alg":"HS256","typ":"JWT"}
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

    /**
     * Generates a signed JWT for the authenticated user.
     * 
     * @param username The user's account name (Subject / sub)
     * @param name The user's display name
     * @param role The user's role (e.g., Plant Manager)
     * @return A real, cryptographically signed JWT token string
     */
    public String generateToken(String username, String name, String role) {
        long nowSeconds = System.currentTimeMillis() / 1000L;
        long expSeconds = nowSeconds + (EXPIRATION_HOURS * 3600L);

        // 1. Base64Url encode Header
        String encodedHeader = base64UrlEncode(HEADER_JSON.getBytes(StandardCharsets.UTF_8));

        // 2. Construct and Base64Url encode Payload claims
        String payloadJson = String.format(
                "{\"sub\":\"%s\",\"name\":\"%s\",\"role\":\"%s\",\"iat\":%d,\"exp\":%d}",
                escapeJson(username),
                escapeJson(name),
                escapeJson(role),
                nowSeconds,
                expSeconds
        );
        String encodedPayload = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));

        // 3. Compute cryptographic HMAC-SHA256 signature
        String dataToSign = encodedHeader + "." + encodedPayload;
        byte[] signatureBytes = computeHmacSha256(dataToSign);
        String encodedSignature = base64UrlEncode(signatureBytes);

        // 4. Return standard 3-part JWT: header.payload.signature
        return dataToSign + "." + encodedSignature;
    }

    /**
     * Validates whether a given token is authentic and not expired.
     * 
     * @param token The JWT token to verify
     * @return true if signature is valid and token is unexpired; false otherwise
     */
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        // Clean optional "Bearer " prefix
        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        String encodedHeader = parts[0];
        String encodedPayload = parts[1];
        String encodedSignature = parts[2];

        // 1. Verify HMAC-SHA256 signature
        String dataToVerify = encodedHeader + "." + encodedPayload;
        byte[] expectedSignatureBytes = computeHmacSha256(dataToVerify);
        byte[] actualSignatureBytes;
        try {
            actualSignatureBytes = Base64.getUrlDecoder().decode(encodedSignature);
        } catch (IllegalArgumentException e) {
            return false;
        }

        // Use constant-time comparison to protect against timing attacks
        if (!MessageDigest.isEqual(expectedSignatureBytes, actualSignatureBytes)) {
            return false; // Signature forgery or tampering detected!
        }

        // 2. Verify Expiration Time
        try {
            byte[] payloadBytes = Base64.getUrlDecoder().decode(encodedPayload);
            String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);

            // Extract "exp":1234567890
            int expIndex = payloadJson.indexOf("\"exp\":");
            if (expIndex != -1) {
                int start = expIndex + 6;
                int end = payloadJson.indexOf("}", start);
                if (end == -1) end = payloadJson.indexOf(",", start);
                if (end != -1) {
                    long expSeconds = Long.parseLong(payloadJson.substring(start, end).trim());
                    long currentSeconds = System.currentTimeMillis() / 1000L;
                    if (currentSeconds > expSeconds) {
                        return false; // Token expired!
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * Extracts the username from the token's 'sub' claim.
     */
    public String getUsernameFromToken(String token) {
        if (!validateToken(token)) return null;

        if (token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        String[] parts = token.split("\\.");
        try {
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);

            int subIndex = payloadJson.indexOf("\"sub\":\"");
            if (subIndex != -1) {
                int start = subIndex + 7;
                int end = payloadJson.indexOf("\"", start);
                if (end != -1) {
                    return payloadJson.substring(start, end);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private byte[] computeHmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    SECRET_KEY.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKeySpec);
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Cryptographic HMAC-SHA256 failure", e);
        }
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"");
    }
}