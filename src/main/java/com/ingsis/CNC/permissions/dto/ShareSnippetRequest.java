package com.ingsis.CNC.permissions.dto;

import java.util.UUID;

public class ShareSnippetRequest {
    private UUID ownerId;
    private UUID targetUserId;
    private UUID snippetId;

    public ShareSnippetRequest() {
    }

    public ShareSnippetRequest(UUID ownerId, UUID targetUserId, UUID snippetId) {
        this.ownerId = ownerId;
        this.targetUserId = targetUserId;
        this.snippetId = snippetId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public UUID getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(UUID targetUserId) {
        this.targetUserId = targetUserId;
    }

    public UUID getSnippetId() {
        return snippetId;
    }

    public void setSnippetId(UUID snippetId) {
        this.snippetId = snippetId;
    }
}
