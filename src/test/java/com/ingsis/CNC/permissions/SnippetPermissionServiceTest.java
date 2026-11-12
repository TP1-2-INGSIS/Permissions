package com.ingsis.CNC.permissions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ingsis.CNC.permissions.dto.CreatePermissionRequest;
import com.ingsis.CNC.permissions.dto.PermissionCheckResponse;
import com.ingsis.CNC.permissions.dto.SnippetPermissionItem;
import com.ingsis.CNC.permissions.dto.SnippetPermissionResponse;
import com.ingsis.CNC.permissions.dto.UserPermissionItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SnippetPermissionServiceTest {

  @Mock private SnippetPermissionRepository repository;

  private SnippetPermissionService service;

  @BeforeEach
  void setUp() {
    service = new SnippetPermissionService(repository);
  }

  @Test
  void createPermissionSuccess() {
    UUID snippetId = UUID.randomUUID();
    String userId = "user-123";
    CreatePermissionRequest request =
        new CreatePermissionRequest(snippetId, userId, SnippetRole.OWNER);

    when(repository.findByUserIdAndSnippetId(userId, snippetId)).thenReturn(Optional.empty());
    when(repository.save(any(SnippetPermission.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    SnippetPermissionResponse response = service.createPermission(request);

    assertThat(response.snippetId()).isEqualTo(snippetId);
    assertThat(response.userId()).isEqualTo(userId);
    assertThat(response.role()).isEqualTo(SnippetRole.OWNER);
  }

  @Test
  void createPermissionThrowsWhenAlreadyExists() {
    UUID snippetId = UUID.randomUUID();
    String userId = "user-123";
    CreatePermissionRequest request =
        new CreatePermissionRequest(snippetId, userId, SnippetRole.READ);

    SnippetPermission existing = new SnippetPermission(userId, snippetId, SnippetRole.READ);
    when(repository.findByUserIdAndSnippetId(userId, snippetId)).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> service.createPermission(request))
        .isInstanceOf(PermissionAlreadyExistsException.class)
        .hasMessageContaining("Permission already exists");
  }

  @Test
  void getPermissionItemsWithRole() {
    String userId = "user-1";
    UUID snippet1 = UUID.randomUUID();
    when(repository.findAllByUserIdAndRole(userId, SnippetRole.READ))
        .thenReturn(List.of(new SnippetPermission(userId, snippet1, SnippetRole.READ)));

    List<SnippetPermissionItem> result = service.getPermissionItems(userId, SnippetRole.READ);

    assertThat(result).containsExactly(new SnippetPermissionItem(snippet1, SnippetRole.READ));
  }

  @Test
  void getPermissionItemsWithoutRole() {
    String userId = "user-1";
    UUID snippet1 = UUID.randomUUID();
    UUID snippet2 = UUID.randomUUID();
    when(repository.findAllByUserId(userId))
        .thenReturn(
            List.of(
                new SnippetPermission(userId, snippet1, SnippetRole.OWNER),
                new SnippetPermission(userId, snippet2, SnippetRole.READ)));

    List<SnippetPermissionItem> result = service.getPermissionItems(userId, null);

    assertThat(result)
        .containsExactly(
            new SnippetPermissionItem(snippet1, SnippetRole.OWNER),
            new SnippetPermissionItem(snippet2, SnippetRole.READ));
  }

  @Test
  void checkPermissionNotFound() {
    String userId = "user-1";
    UUID snippetId = UUID.randomUUID();
    when(repository.findByUserIdAndSnippetId(userId, snippetId)).thenReturn(Optional.empty());

    PermissionCheckResponse response =
        service.checkPermission(userId, snippetId, PermissionAction.READ);

    assertThat(response.allowed()).isFalse();
    assertThat(response.role()).isNull();
  }

  @Test
  void checkPermissionOwnerAllowedForAnyAction() {
    String userId = "user-1";
    UUID snippetId = UUID.randomUUID();
    when(repository.findByUserIdAndSnippetId(userId, snippetId))
        .thenReturn(Optional.of(new SnippetPermission(userId, snippetId, SnippetRole.OWNER)));

    assertThat(service.checkPermission(userId, snippetId, PermissionAction.READ))
        .isEqualTo(new PermissionCheckResponse(true, SnippetRole.OWNER));
    assertThat(service.checkPermission(userId, snippetId, PermissionAction.WRITE))
        .isEqualTo(new PermissionCheckResponse(true, SnippetRole.OWNER));
    assertThat(service.checkPermission(userId, snippetId, PermissionAction.SHARE))
        .isEqualTo(new PermissionCheckResponse(true, SnippetRole.OWNER));
  }

  @Test
  void checkPermissionReadRoleOnlyAllowedForRead() {
    String userId = "user-1";
    UUID snippetId = UUID.randomUUID();
    when(repository.findByUserIdAndSnippetId(userId, snippetId))
        .thenReturn(Optional.of(new SnippetPermission(userId, snippetId, SnippetRole.READ)));

    assertThat(service.checkPermission(userId, snippetId, PermissionAction.READ))
        .isEqualTo(new PermissionCheckResponse(true, SnippetRole.READ));
    assertThat(service.checkPermission(userId, snippetId, PermissionAction.WRITE))
        .isEqualTo(new PermissionCheckResponse(false, SnippetRole.READ));
    assertThat(service.checkPermission(userId, snippetId, PermissionAction.SHARE))
        .isEqualTo(new PermissionCheckResponse(false, SnippetRole.READ));
  }

  @Test
  void getUsersOfSnippet() {
    UUID snippetId = UUID.randomUUID();
    when(repository.findAllBySnippetId(snippetId))
        .thenReturn(
            List.of(
                new SnippetPermission("user-1", snippetId, SnippetRole.OWNER),
                new SnippetPermission("user-2", snippetId, SnippetRole.READ)));

    List<UserPermissionItem> users = service.getUsersOfSnippet(snippetId);

    assertThat(users)
        .containsExactly(
            new UserPermissionItem("user-1", SnippetRole.OWNER),
            new UserPermissionItem("user-2", SnippetRole.READ));
  }

  @Test
  void deletePermissionsForSnippet() {
    UUID snippetId = UUID.randomUUID();

    service.deletePermissionsForSnippet(snippetId);

    verify(repository).deleteAllBySnippetId(snippetId);
  }
}
