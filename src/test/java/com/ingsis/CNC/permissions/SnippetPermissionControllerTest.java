package com.ingsis.CNC.permissions;

import com.ingsis.CNC.permissions.dto.CreatePermissionRequest;
import com.ingsis.CNC.permissions.dto.ShareSnippetRequest;
import com.ingsis.CNC.permissions.dto.SnippetPermissionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SnippetPermissionControllerTest {
    private final SnippetPermissionService service = mock(SnippetPermissionService.class);
    private final SnippetPermissionController controller = new SnippetPermissionController(service);
    private final UUID userId = UUID.randomUUID();
    private final UUID snippetId = UUID.randomUUID();
    private final SnippetPermissionResponse permission = new SnippetPermissionResponse(UUID.randomUUID(), userId, snippetId, SnippetRole.OWNER, LocalDateTime.now());

    @Test
    void delegatesCreateShareAndList() {
        CreatePermissionRequest create = new CreatePermissionRequest(userId, snippetId, SnippetRole.OWNER);
        ShareSnippetRequest share = new ShareSnippetRequest(userId, UUID.randomUUID(), snippetId);
        when(service.createOrUpdatePermission(create)).thenReturn(permission);
        when(service.shareSnippet(share)).thenReturn(permission);
        when(service.getSnippetIdsByUser(userId, SnippetRole.OWNER)).thenReturn(List.of(snippetId));

        assertEquals(permission, controller.createPermission(create).getBody());
        assertEquals(permission, controller.shareSnippet(share).getBody());
        assertEquals(List.of(snippetId), controller.getSnippetsForUser(userId, SnippetRole.OWNER).getBody());
    }

    @Test
    void handlesLookupReadWriteAndDelete() {
        when(service.getPermission(userId, snippetId)).thenReturn(Optional.of(permission));
        assertEquals(HttpStatus.OK, controller.getPermission(userId, snippetId).getStatusCode());
        when(service.getPermission(userId, snippetId)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND, controller.getPermission(userId, snippetId).getStatusCode());

        when(service.canRead(userId, snippetId)).thenReturn(true);
        when(service.canWrite(userId, snippetId)).thenReturn(false);
        assertTrue(controller.canRead(userId, snippetId).getBody().get("canRead"));
        assertFalse(controller.canWrite(userId, snippetId).getBody().get("canWrite"));
        assertEquals(HttpStatus.NO_CONTENT, controller.deleteSnippetPermissions(snippetId).getStatusCode());
    }
}
