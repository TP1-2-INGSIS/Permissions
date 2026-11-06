package com.ingsis.CNC.permissions.dto;

import com.ingsis.CNC.permissions.SnippetRole;
import java.util.UUID;

public record CreatePermissionRequest(UUID userId, UUID snippetId, SnippetRole snippetRole) {}
