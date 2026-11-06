package com.ingsis.CNC.permissions.dto;

import java.util.UUID;

public record ShareSnippetRequest(UUID ownerId, UUID targetUserId, UUID snippetId) {}
