package com.ingsis.CNC.auth.dto;

import java.util.UUID;

public record AuthResponse(UUID id, String userName, String email) {}
