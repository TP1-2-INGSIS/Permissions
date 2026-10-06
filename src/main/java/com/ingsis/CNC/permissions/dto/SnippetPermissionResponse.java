package com.ingsis.CNC.permissions.dto;

import com.ingsis.CNC.permissions.SnippetPermission;
import com.ingsis.CNC.permissions.SnippetRole;

import java.time.LocalDateTime;
import java.util.UUID;

public class SnippetPermissionResponse {
    private UUID id;
    private UUID userId;
    private UUID snippetId;
    private SnippetRole role;
    private LocalDateTime createdAt;

    public SnippetPermissionResponse() {
    }

    public SnippetPermissionResponse(UUID id, UUID userId, UUID snippetId, SnippetRole role, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.snippetId = snippetId;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static SnippetPermissionResponse fromEntity(SnippetPermission permission) {
        return new SnippetPermissionResponse(
            permission.getId(),
            permission.getUserId(),
            permission.getSnippetId(),
            permission.getRole(),
            permission.getCreatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getSnippetId() {
        return snippetId;
    }

    public void setSnippetId(UUID snippetId) {
        this.snippetId = snippetId;
    }

    public SnippetRole getRole() {
        return role;
    }

    public void setRole(SnippetRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
