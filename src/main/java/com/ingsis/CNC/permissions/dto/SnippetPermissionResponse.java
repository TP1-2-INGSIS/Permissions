package com.ingsis.CNC.permissions.dto;

import com.ingsis.CNC.permissions.SnippetRole;
import java.time.LocalDateTime;
import java.util.UUID;

public record SnippetPermissionResponse(
    UUID id, UUID userId, UUID snippetId, SnippetRole role, LocalDateTime createdAt) {}
