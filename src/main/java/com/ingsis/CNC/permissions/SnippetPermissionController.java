package com.ingsis.CNC.permissions;

import com.ingsis.CNC.permissions.dto.CreatePermissionRequest;
import com.ingsis.CNC.permissions.dto.PermissionCheckResponse;
import com.ingsis.CNC.permissions.dto.ShareSnippetRequest;
import com.ingsis.CNC.permissions.dto.SnippetPermissionItem;
import com.ingsis.CNC.permissions.dto.SnippetPermissionResponse;
import com.ingsis.CNC.permissions.dto.UserPermissionItem;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/permissions")
public class SnippetPermissionController {

  private final SnippetPermissionService service;

  public SnippetPermissionController(SnippetPermissionService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<SnippetPermissionResponse> createPermission(
      @RequestBody CreatePermissionRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.createPermission(request));
  }

  @GetMapping
  public ResponseEntity<List<SnippetPermissionItem>> getPermissions(
      @RequestParam String userId, @RequestParam(required = false) SnippetRole role) {
    return ResponseEntity.ok(service.getPermissionItems(userId, role));
  }

  @GetMapping("/check")
  public ResponseEntity<PermissionCheckResponse> checkPermission(
      @RequestParam String userId,
      @RequestParam UUID snippetId,
      @RequestParam(required = false, defaultValue = "READ") PermissionAction action) {
    return ResponseEntity.ok(service.checkPermission(userId, snippetId, action));
  }

  @GetMapping({"/snippets/{snippetId}", "/snippet/{snippetId}"})
  public ResponseEntity<List<UserPermissionItem>> getUsersOfSnippet(@PathVariable UUID snippetId) {
    return ResponseEntity.ok(service.getUsersOfSnippet(snippetId));
  }

  @DeleteMapping({"/snippets/{snippetId}", "/snippet/{snippetId}"})
  public ResponseEntity<Void> deleteSnippetPermissions(@PathVariable UUID snippetId) {
    service.deletePermissionsForSnippet(snippetId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/share")
  public ResponseEntity<SnippetPermissionResponse> shareSnippet(
      @RequestBody ShareSnippetRequest request) {
    return ResponseEntity.ok(service.shareSnippet(request));
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<UUID>> getSnippetsForUser(
      @PathVariable String userId, @RequestParam(required = false) SnippetRole role) {
    return ResponseEntity.ok(service.getSnippetIdsByUser(userId, role));
  }

  @GetMapping("/can-read")
  public ResponseEntity<Map<String, Boolean>> canRead(
      @RequestParam String userId, @RequestParam UUID snippetId) {
    return ResponseEntity.ok(Map.of("canRead", service.canRead(userId, snippetId)));
  }

  @GetMapping("/can-write")
  public ResponseEntity<Map<String, Boolean>> canWrite(
      @RequestParam String userId, @RequestParam UUID snippetId) {
    return ResponseEntity.ok(Map.of("canWrite", service.canWrite(userId, snippetId)));
  }
}
