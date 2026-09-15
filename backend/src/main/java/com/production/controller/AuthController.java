package com.production.controller;

import com.production.model.LoginRequest;
import com.production.model.LoginResponse;
import com.production.security.JwtService;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;

import java.util.Map;

/**
 * Controller for User Authentication.
 * Issues real cryptographically signed JWT tokens and validates them.
 */
@Controller("/api/auth")
public class AuthController {

    private final JwtService jwtService;

    @Inject
    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * POST /api/auth/login
     * Authenticates user and returns a signed JSON Web Token (JWT).
     */
    @Post("/login")
    public HttpResponse<LoginResponse> login(@Body LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return HttpResponse.badRequest(new LoginResponse(false, null, null, null, null, "Username and password required"));
        }

        String username = request.getUsername().trim();
        String password = request.getPassword().trim();

        // Valid credentials verification
        if (("admin".equalsIgnoreCase(username) || "vaishnavi".equalsIgnoreCase(username)) && "admin123".equals(password)) {
            String displayName = "vaishnavi".equalsIgnoreCase(username) ? "Vaishnavi" : "Production Administrator";
            String role = "Plant Manager";

            // Issue real cryptographically signed JWT token (HMAC-SHA256)
            String jwtToken = jwtService.generateToken(username, displayName, role);

            return HttpResponse.ok(new LoginResponse(
                    true,
                    jwtToken,
                    username,
                    displayName,
                    role,
                    "Login successful. Real JWT issued."
            ));
        }

        if ("admin123".equals(password)) {
            String displayName = username;
            String role = "Production Engineer";
            String jwtToken = jwtService.generateToken(username, displayName, role);

            return HttpResponse.ok(new LoginResponse(
                    true,
                    jwtToken,
                    username,
                    displayName,
                    role,
                    "Login successful. Real JWT issued."
            ));
        }

        return HttpResponse.unauthorized().body(new LoginResponse(
                false,
                null,
                null,
                null,
                null,
                "Invalid credentials. Hint: username 'admin' and password 'admin123'"
        ));
    }

    /**
     * GET /api/auth/validate
     * Validates whether the incoming JWT token signature is authentic and unexpired.
     */
    @Get("/validate")
    public HttpResponse<?> validateToken(@Header(HttpHeaders.AUTHORIZATION) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return HttpResponse.unauthorized().body(Map.of("valid", false, "error", "Missing Authorization header"));
        }

        String token = authHeader.substring(7).trim();
        boolean isValid = jwtService.validateToken(token);

        if (isValid) {
            String user = jwtService.getUsernameFromToken(token);
            return HttpResponse.ok(Map.of("valid", true, "username", user));
        } else {
            return HttpResponse.unauthorized().body(Map.of("valid", false, "error", "Invalid or expired JWT signature"));
        }
    }
}