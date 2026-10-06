package com.ingsis.CNC.permissions;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "snippet_permissions",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_snippet", columnNames = {"user_id", "snippet_id"})
    }
)
public class SnippetPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "snippet_id", nullable = false)
    private UUID snippetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private SnippetRole role;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public SnippetPermission() {
    }

    public SnippetPermission(UUID userId, UUID snippetId, SnippetRole role) {
        this.userId = userId;
        this.snippetId = snippetId;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
