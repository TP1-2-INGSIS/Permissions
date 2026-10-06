package com.ingsis.CNC.permissions.dto;

import com.ingsis.CNC.permissions.SnippetRole;
import java.util.UUID;

public class CreatePermissionRequest {
    private UUID userId;
    private UUID snippetId;
    private SnippetRole role;

    public CreatePermissionRequest() {
    }

    public CreatePermissionRequest(UUID userId, UUID snippetId, SnippetRole role) {
        this.userId = userId;
        this.snippetId = snippetId;
        this.role = role;
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
}
