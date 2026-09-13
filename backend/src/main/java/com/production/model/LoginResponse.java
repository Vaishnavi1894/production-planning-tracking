package com.production.model;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Response payload returned after authentication.
 */
@Serdeable
public class LoginResponse {
    private boolean success;
    private String token;
    private String username;
    private String name;
    private String role;
    private String message;

    public LoginResponse() {
    }

    public LoginResponse(boolean success, String token, String username, String name, String role, String message) {
        this.success = success;
        this.token = token;
        this.username = username;
        this.name = name;
        this.role = role;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
