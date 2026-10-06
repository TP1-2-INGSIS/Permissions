package com.ingsis.CNC.auth.dto;

import java.util.UUID;

public class RegisterResponse {
    private UUID id;
    private String userName;
    private String email;

    public RegisterResponse() {
    }

    public RegisterResponse(UUID id, String userName, String email) {
        this.id = id;
        this.userName = userName;
        this.email = email;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
