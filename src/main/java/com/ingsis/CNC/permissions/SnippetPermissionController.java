package com.ingsis.CNC.permissions;

import com.ingsis.CNC.permissions.dto.CreatePermissionRequest;
import com.ingsis.CNC.permissions.dto.ShareSnippetRequest;
import com.ingsis.CNC.permissions.dto.SnippetPermissionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/permissions")
public class SnippetPermissionController {

    private final SnippetPermissionService service;

    public SnippetPermissionController(SnippetPermissionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SnippetPermissionResponse> createPermission(@RequestBody CreatePermissionRequest request) {
        return ResponseEntity.ok(service.createOrUpdatePermission(request));
    }

    @PostMapping("/share")
    public ResponseEntity<SnippetPermissionResponse> shareSnippet(@RequestBody ShareSnippetRequest request) {
        return ResponseEntity.ok(service.shareSnippet(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UUID>> getSnippetsForUser(
            @PathVariable UUID userId,
            @RequestParam(required = false) SnippetRole role) {
        return ResponseEntity.ok(service.getSnippetIdsByUser(userId, role));
    }

    @GetMapping("/check")
    public ResponseEntity<SnippetPermissionResponse> getPermission(
            @RequestParam UUID userId,
            @RequestParam UUID snippetId) {
        return service.getPermission(userId, snippetId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/can-read")
    public ResponseEntity<Map<String, Boolean>> canRead(
            @RequestParam UUID userId,
            @RequestParam UUID snippetId) {
        return ResponseEntity.ok(Map.of("canRead", service.canRead(userId, snippetId)));
    }

    @GetMapping("/can-write")
    public ResponseEntity<Map<String, Boolean>> canWrite(
            @RequestParam UUID userId,
            @RequestParam UUID snippetId) {
        return ResponseEntity.ok(Map.of("canWrite", service.canWrite(userId, snippetId)));
    }

    @DeleteMapping("/snippet/{snippetId}")
    public ResponseEntity<Void> deleteSnippetPermissions(@PathVariable UUID snippetId) {
        service.deletePermissionsForSnippet(snippetId);
        return ResponseEntity.noContent().build();
    }
}
