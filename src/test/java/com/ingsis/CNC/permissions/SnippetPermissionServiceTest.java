package com.ingsis.CNC.permissions;

import com.ingsis.CNC.permissions.dto.CreatePermissionRequest;
import com.ingsis.CNC.permissions.dto.ShareSnippetRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnippetPermissionServiceTest {
    @Mock SnippetPermissionRepository repository;
    @InjectMocks SnippetPermissionService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID ownerId = UUID.randomUUID();
    private final UUID targetId = UUID.randomUUID();
    private final UUID snippetId = UUID.randomUUID();

    @Test
    void createsAndUpdatesPermission() {
        when(repository.findByUserIdAndSnippetId(userId, snippetId)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
        assertEquals(SnippetRole.GUEST, service.createOrUpdatePermission(new CreatePermissionRequest(userId, snippetId, SnippetRole.GUEST)).role());

        SnippetPermission existing = new SnippetPermission(userId, snippetId, SnippetRole.GUEST);
        when(repository.findByUserIdAndSnippetId(userId, snippetId)).thenReturn(Optional.of(existing));
        assertEquals(SnippetRole.OWNER, service.createOrUpdatePermission(new CreatePermissionRequest(userId, snippetId, SnippetRole.OWNER)).role());
    }

    @Test
    void shareRequiresOwnerAndCreatesOrUpdatesGuestWithoutChangingOwner() {
        ShareSnippetRequest request = new ShareSnippetRequest(ownerId, targetId, snippetId);
        when(repository.findByUserIdAndSnippetId(ownerId, snippetId)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.shareSnippet(request));

        when(repository.findByUserIdAndSnippetId(ownerId, snippetId)).thenReturn(Optional.of(new SnippetPermission(ownerId, snippetId, SnippetRole.GUEST)));
        assertThrows(ResponseStatusException.class, () -> service.shareSnippet(request));

        when(repository.findByUserIdAndSnippetId(ownerId, snippetId)).thenReturn(Optional.of(new SnippetPermission(ownerId, snippetId, SnippetRole.OWNER)));
        when(repository.findByUserIdAndSnippetId(targetId, snippetId)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
        assertEquals(SnippetRole.GUEST, service.shareSnippet(request).role());

        SnippetPermission existingGuest = new SnippetPermission(targetId, snippetId, SnippetRole.OWNER);
        when(repository.findByUserIdAndSnippetId(targetId, snippetId)).thenReturn(Optional.of(existingGuest));
        assertEquals(SnippetRole.OWNER, service.shareSnippet(request).role());
        existingGuest.setRole(SnippetRole.GUEST);
        when(repository.findByUserIdAndSnippetId(targetId, snippetId)).thenReturn(Optional.of(existingGuest));
        assertEquals(SnippetRole.GUEST, service.shareSnippet(request).role());
    }

    @Test
    void listsPermissionsWithAndWithoutRoleFilter() {
        SnippetPermission permission = new SnippetPermission(userId, snippetId, SnippetRole.OWNER);
        when(repository.findAllByUserIdAndRole(userId, SnippetRole.OWNER)).thenReturn(List.of(permission));
        when(repository.findAllByUserId(userId)).thenReturn(List.of(permission));
        assertEquals(List.of(snippetId), service.getSnippetIdsByUser(userId, SnippetRole.OWNER));
        assertEquals(List.of(snippetId), service.getSnippetIdsByUser(userId, null));
    }

    @Test
    void checksReadWriteAndPermissionLookup() {
        SnippetPermission owner = new SnippetPermission(userId, snippetId, SnippetRole.OWNER);
        when(repository.findByUserIdAndSnippetId(userId, snippetId)).thenReturn(Optional.of(owner));
        assertTrue(service.getPermission(userId, snippetId).isPresent());
        assertTrue(service.canRead(userId, snippetId));
        assertTrue(service.canWrite(userId, snippetId));

        owner.setRole(SnippetRole.GUEST);
        assertFalse(service.canWrite(userId, snippetId));
        when(repository.findByUserIdAndSnippetId(userId, snippetId)).thenReturn(Optional.empty());
        assertTrue(service.getPermission(userId, snippetId).isEmpty());
        assertFalse(service.canRead(userId, snippetId));
        assertFalse(service.canWrite(userId, snippetId));
    }

    @Test
    void deletesPermissionsForSnippet() {
        service.deletePermissionsForSnippet(snippetId);
        verify(repository).deleteAllBySnippetId(snippetId);
    }
}
