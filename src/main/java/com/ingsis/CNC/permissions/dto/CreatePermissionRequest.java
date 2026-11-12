package com.ingsis.CNC.permissions.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ingsis.CNC.permissions.SnippetRole;
import java.util.UUID;

public record CreatePermissionRequest(
    UUID snippetId, String userId, SnippetRole role, SnippetRole snippetRole) {

  @JsonCreator
  public CreatePermissionRequest(
      @JsonProperty("snippetId") UUID snippetId,
      @JsonProperty("userId") String userId,
      @JsonProperty("role") SnippetRole role,
      @JsonProperty("snippetRole") SnippetRole snippetRole) {
    this.snippetId = snippetId;
    this.userId = userId;
    this.role = role != null ? role : snippetRole;
    this.snippetRole = snippetRole != null ? snippetRole : role;
  }

  public CreatePermissionRequest(UUID snippetId, String userId, SnippetRole role) {
    this(snippetId, userId, role, role);
  }

  public SnippetRole effectiveRole() {
    if (role != null) {
      return role;
    }
    return snippetRole;
  }
}
