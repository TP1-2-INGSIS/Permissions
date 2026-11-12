package com.ingsis.CNC.permissions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "snippet_permissions",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_user_snippet",
          columnNames = {"user_id", "snippet_id"})
    })
public class SnippetPermission {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(name = "snippet_id", nullable = false)
  private UUID snippetId;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 32)
  private SnippetRole role;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  public SnippetPermission() {}

  public SnippetPermission(String userId, UUID snippetId, SnippetRole role) {
    this.userId = userId;
    this.snippetId = snippetId;
    this.role = role;
    this.createdAt = LocalDateTime.now();
  }

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
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
