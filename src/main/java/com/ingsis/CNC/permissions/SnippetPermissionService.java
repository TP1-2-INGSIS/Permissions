package com.ingsis.CNC.permissions;

import com.ingsis.CNC.permissions.dto.CreatePermissionRequest;
import com.ingsis.CNC.permissions.dto.PermissionCheckResponse;
import com.ingsis.CNC.permissions.dto.ShareSnippetRequest;
import com.ingsis.CNC.permissions.dto.SnippetPermissionItem;
import com.ingsis.CNC.permissions.dto.SnippetPermissionResponse;
import com.ingsis.CNC.permissions.dto.UserPermissionItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
  public SnippetPermissionResponse createPermission(CreatePermissionRequest request) {
    if (repository.findByUserIdAndSnippetId(request.userId(), request.snippetId()).isPresent()) {
      throw new PermissionAlreadyExistsException("Permission already exists");
    }
    SnippetRole role = request.effectiveRole() != null ? request.effectiveRole() : SnippetRole.READ;
    SnippetPermission permission =
        new SnippetPermission(request.userId(), request.snippetId(), role);
    SnippetPermission saved = repository.save(permission);
    return toResponse(saved);
  }

  public List<SnippetPermissionItem> getPermissionItems(String userId, SnippetRole role) {
    List<SnippetPermission> permissions;
    if (role != null) {
      permissions = repository.findAllByUserIdAndRole(userId, role);
    } else {
      permissions = repository.findAllByUserId(userId);
    }
    return permissions.stream()
        .map(p -> new SnippetPermissionItem(p.getSnippetId(), p.getRole()))
        .toList();
  }

  public PermissionCheckResponse checkPermission(
      String userId, UUID snippetId, PermissionAction action) {
    Optional<SnippetPermission> permissionOpt =
        repository.findByUserIdAndSnippetId(userId, snippetId);
    if (permissionOpt.isEmpty()) {
      return new PermissionCheckResponse(false, null);
    }

    SnippetRole role = permissionOpt.get().getRole();
    PermissionAction effectiveAction = action != null ? action : PermissionAction.READ;
    boolean allowed = false;
    if (role == SnippetRole.OWNER) {
      allowed = true;
    } else if (role == SnippetRole.READ) {
      allowed = (effectiveAction == PermissionAction.READ);
    }

    return new PermissionCheckResponse(allowed, role);
  }

  public List<UserPermissionItem> getUsersOfSnippet(UUID snippetId) {
    return repository.findAllBySnippetId(snippetId).stream()
        .map(p -> new UserPermissionItem(p.getUserId(), p.getRole()))
        .toList();
  }

  @Transactional
  public void deletePermissionsForSnippet(UUID snippetId) {
    repository.deleteAllBySnippetId(snippetId);
  }

  @Transactional
  public SnippetPermissionResponse createOrUpdatePermission(CreatePermissionRequest request) {
    SnippetRole role = request.effectiveRole() != null ? request.effectiveRole() : SnippetRole.READ;
    SnippetPermission permission =
        repository
            .findByUserIdAndSnippetId(request.userId(), request.snippetId())
            .map(existing -> {
              existing.setRole(role);
              return existing;
            })
            .orElseGet(() -> new SnippetPermission(request.userId(), request.snippetId(), role));

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
                existing.setRole(SnippetRole.READ);
              }
              return existing;
            })
            .orElseGet(() ->
                new SnippetPermission(
                    request.targetUserId(), request.snippetId(), SnippetRole.READ));

    SnippetPermission saved = repository.save(guestPermission);
    return toResponse(saved);
  }

  public List<UUID> getSnippetIdsByUser(String userId, SnippetRole role) {
    List<SnippetPermission> permissions;
    if (role != null) {
      permissions = repository.findAllByUserIdAndRole(userId, role);
    } else {
      permissions = repository.findAllByUserId(userId);
    }
    return permissions.stream().map(SnippetPermission::getSnippetId).toList();
  }

  public Optional<SnippetPermissionResponse> getPermission(String userId, UUID snippetId) {
    return repository.findByUserIdAndSnippetId(userId, snippetId).map(this::toResponse);
  }

  public boolean canRead(String userId, UUID snippetId) {
    return repository.findByUserIdAndSnippetId(userId, snippetId).isPresent();
  }

  public boolean canWrite(String userId, UUID snippetId) {
    return repository
        .findByUserIdAndSnippetId(userId, snippetId)
        .map(p -> p.getRole() == SnippetRole.OWNER)
        .orElse(false);
  }

  private SnippetPermissionResponse toResponse(SnippetPermission permission) {
    return new SnippetPermissionResponse(
        permission.getId(),
        permission.getUserId(),
        permission.getSnippetId(),
        permission.getRole(),
        permission.getCreatedAt());
  }
}
