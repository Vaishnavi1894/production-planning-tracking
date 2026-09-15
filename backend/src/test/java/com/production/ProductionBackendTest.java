package com.production;

import com.production.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

class ProductionBackendTest {

    @Test
    void testBasicCalculation() {
        double planned = 500;
        double produced = 450;
        double diff = produced - planned;
        double achievement = (produced / planned) * 100.0;
        Assertions.assertEquals(-50, diff);
        Assertions.assertEquals(90.0, achievement);
    }

    @Test
    void testJwtTokenGenerationAndValidation() {
        JwtService jwtService = new JwtService();
        String token = jwtService.generateToken("vaishnavi", "Vaishnavi", "Plant Manager");

        // 1. Verify token is formed as Header.Payload.Signature (3 parts)
        Assertions.assertNotNull(token);
        String[] parts = token.split("\\.");
        Assertions.assertEquals(3, parts.length, "JWT must consist of exactly 3 parts separated by dots");

        // 2. Verify signature validity
        Assertions.assertTrue(jwtService.validateToken(token), "Generated JWT token must pass signature verification");

        // 3. Verify extracted subject
        String extractedUser = jwtService.getUsernameFromToken(token);
        Assertions.assertEquals("vaishnavi", extractedUser);

        // 4. Verify tampering detection (modified payload)
        String forgedToken = parts[0] + ".eyJzdWIiOiJoYWNrZXIifQ." + parts[2];
        Assertions.assertFalse(jwtService.validateToken(forgedToken), "Forged/tampered token must be rejected");
    }
}