package com.ingsis.CNC.permissions.dto;

import java.util.UUID;

public record ShareSnippetRequest(String ownerId, String targetUserId, UUID snippetId) {}
