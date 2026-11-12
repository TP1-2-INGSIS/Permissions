package com.ingsis.CNC.permissions.dto;

import com.ingsis.CNC.permissions.SnippetRole;

public record PermissionCheckResponse(boolean allowed, SnippetRole role) {}
