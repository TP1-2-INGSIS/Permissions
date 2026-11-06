package com.ingsis.CNC.permissions;

import com.ingsis.CNC.permissions.dto.CreatePermissionRequest;
import com.ingsis.CNC.permissions.dto.ShareSnippetRequest;
import com.ingsis.CNC.permissions.dto.SnippetPermissionResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SnippetPermissionService {

  private final SnippetPermissionRepository repository;

  public SnippetPermissionService(SnippetPermissionRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public SnippetPermissionResponse createOrUpdatePermission(CreatePermissionRequest request) {
    SnippetPermission permission =
        repository
            .findByUserIdAndSnippetId(request.userId(), request.snippetId())
            .map(existing -> {
              existing.setRole(request.snippetRole());
              return existing;
            })
            .orElseGet(() ->
                new SnippetPermission(
                    request.userId(), request.snippetId(), request.snippetRole()));

    SnippetPermission saved = repository.save(permission);
    return toResponse(saved);
  }

  @Transactional
  public SnippetPermissionResponse shareSnippet(ShareSnippetRequest request) {
    SnippetPermission ownerPermission =
        repository
            .findByUserIdAndSnippetId(request.ownerId(), request.snippetId())
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Owner not found or has no access"));

    if (ownerPermission.getRole() != SnippetRole.OWNER) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Only the owner can share this snippet");
    }

    SnippetPermission guestPermission =
        repository
            .findByUserIdAndSnippetId(request.targetUserId(), request.snippetId())
            .map(existing -> {
              if (existing.getRole() != SnippetRole.OWNER) {
                existing.setRole(SnippetRole.GUEST);
              }
              return existing;
            })
            .orElseGet(() ->
                new SnippetPermission(
                    request.targetUserId(), request.snippetId(), SnippetRole.GUEST));

    SnippetPermission saved = repository.save(guestPermission);
    return toResponse(saved);
  }

  public List<UUID> getSnippetIdsByUser(UUID userId, SnippetRole role) {
    List<SnippetPermission> permissions;
    if (role != null) {
      permissions = repository.findAllByUserIdAndRole(userId, role);
    } else {
      permissions = repository.findAllByUserId(userId);
    }
    return permissions.stream().map(SnippetPermission::getSnippetId).collect(Collectors.toList());
  }

  public Optional<SnippetPermissionResponse> getPermission(UUID userId, UUID snippetId) {
    return repository.findByUserIdAndSnippetId(userId, snippetId).map(this::toResponse);
  }

  private SnippetPermissionResponse toResponse(SnippetPermission permission) {
    return new SnippetPermissionResponse(
        permission.getId(),
        permission.getUserId(),
        permission.getSnippetId(),
        permission.getRole(),
        permission.getCreatedAt());
  }

  public boolean canRead(UUID userId, UUID snippetId) {
    return repository.findByUserIdAndSnippetId(userId, snippetId).isPresent();
  }

  public boolean canWrite(UUID userId, UUID snippetId) {
    return repository
        .findByUserIdAndSnippetId(userId, snippetId)
        .map(p -> p.getRole() == SnippetRole.OWNER)
        .orElse(false);
  }

  @Transactional
  public void deletePermissionsForSnippet(UUID snippetId) {
    repository.deleteAllBySnippetId(snippetId);
  }
}
