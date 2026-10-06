package com.ingsis.CNC.permissions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SnippetPermissionRepository extends JpaRepository<SnippetPermission, UUID> {
    Optional<SnippetPermission> findByUserIdAndSnippetId(UUID userId, UUID snippetId);
    List<SnippetPermission> findAllByUserId(UUID userId);
    List<SnippetPermission> findAllByUserIdAndRole(UUID userId, SnippetRole role);
    List<SnippetPermission> findAllBySnippetId(UUID snippetId);
    void deleteByUserIdAndSnippetId(UUID userId, UUID snippetId);
    void deleteAllBySnippetId(UUID snippetId);
}
