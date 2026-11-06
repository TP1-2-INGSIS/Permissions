package com.ingsis.CNC.permissions.dto;

import com.ingsis.CNC.permissions.SnippetRole;

public record UserPermissionItem(String userId, SnippetRole role) {}
