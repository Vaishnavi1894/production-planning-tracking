package com.production.controller;

import com.production.model.LoginRequest;
import com.production.model.LoginResponse;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;

/**
 * Controller for User Authentication.
 * Handles the Login screen verification.
 */
@Controller("/api/auth")
public class AuthController {

    @Post("/login")
    public HttpResponse<LoginResponse> login(@Body LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            return HttpResponse.badRequest(new LoginResponse(false, null, null, null, null, "Username and password required"));
        }

        String username = request.getUsername().trim();
        String password = request.getPassword().trim();

        // Simple credentials check: allows "admin" or "vaishnavi" with password "admin123" or any valid combo
        if (("admin".equalsIgnoreCase(username) || "vaishnavi".equalsIgnoreCase(username)) && "admin123".equals(password)) {
            String displayName = "vaishnavi".equalsIgnoreCase(username) ? "Vaishnavi" : "Production Administrator";
            return HttpResponse.ok(new LoginResponse(
                    true,
                    "jwt-mock-token-" + System.currentTimeMillis(),
                    username,
                    displayName,
                    "Plant Manager",
                    "Login successful"
            ));
        }

        // Demo fallback: If user enters username with correct demo hint
        if ("admin123".equals(password)) {
            return HttpResponse.ok(new LoginResponse(
                    true,
                    "jwt-mock-token-" + System.currentTimeMillis(),
                    username,
                    username,
                    "Production Engineer",
                    "Login successful"
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
}
