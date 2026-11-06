package com.ingsis.CNC.permissions.dto;

import com.ingsis.CNC.permissions.SnippetRole;
import java.util.UUID;

public record SnippetPermissionItem(UUID snippetId, SnippetRole role) {}
