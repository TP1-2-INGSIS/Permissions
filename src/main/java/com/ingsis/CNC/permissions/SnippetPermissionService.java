package com.ingsis.CNC.permissions;

import com.ingsis.CNC.permissions.dto.CreatePermissionRequest;
import com.ingsis.CNC.permissions.dto.ShareSnippetRequest;
import com.ingsis.CNC.permissions.dto.SnippetPermissionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SnippetPermissionService {

    private final SnippetPermissionRepository repository;

    public SnippetPermissionService(SnippetPermissionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SnippetPermissionResponse createOrUpdatePermission(CreatePermissionRequest request) {
        SnippetPermission permission = repository.findByUserIdAndSnippetId(request.getUserId(), request.getSnippetId())
                .map(existing -> {
                    existing.setRole(request.getRole());
                    return existing;
                })
                .orElseGet(() -> new SnippetPermission(request.getUserId(), request.getSnippetId(), request.getRole()));

        SnippetPermission saved = repository.save(permission);
        return SnippetPermissionResponse.fromEntity(saved);
    }

    @Transactional
    public SnippetPermissionResponse shareSnippet(ShareSnippetRequest request) {
        SnippetPermission ownerPermission = repository.findByUserIdAndSnippetId(request.getOwnerId(), request.getSnippetId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Owner not found or has no access"));

        if (ownerPermission.getRole() != SnippetRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner can share this snippet");
        }

        SnippetPermission guestPermission = repository.findByUserIdAndSnippetId(request.getTargetUserId(), request.getSnippetId())
                .map(existing -> {
                    if (existing.getRole() != SnippetRole.OWNER) {
                        existing.setRole(SnippetRole.GUEST);
                    }
                    return existing;
                })
                .orElseGet(() -> new SnippetPermission(request.getTargetUserId(), request.getSnippetId(), SnippetRole.GUEST));

        SnippetPermission saved = repository.save(guestPermission);
        return SnippetPermissionResponse.fromEntity(saved);
    }

    public List<UUID> getSnippetIdsByUser(UUID userId, SnippetRole role) {
        List<SnippetPermission> permissions;
        if (role != null) {
            permissions = repository.findAllByUserIdAndRole(userId, role);
        } else {
            permissions = repository.findAllByUserId(userId);
        }
        return permissions.stream()
                .map(SnippetPermission::getSnippetId)
                .collect(Collectors.toList());
    }

    public Optional<SnippetPermissionResponse> getPermission(UUID userId, UUID snippetId) {
        return repository.findByUserIdAndSnippetId(userId, snippetId)
                .map(SnippetPermissionResponse::fromEntity);
    }

    public boolean canRead(UUID userId, UUID snippetId) {
        return repository.findByUserIdAndSnippetId(userId, snippetId).isPresent();
    }

    public boolean canWrite(UUID userId, UUID snippetId) {
        return repository.findByUserIdAndSnippetId(userId, snippetId)
                .map(p -> p.getRole() == SnippetRole.OWNER)
                .orElse(false);
    }

    @Transactional
    public void deletePermissionsForSnippet(UUID snippetId) {
        repository.deleteAllBySnippetId(snippetId);
    }
}
