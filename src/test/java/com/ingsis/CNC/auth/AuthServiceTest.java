package com.ingsis.CNC.auth;

import com.ingsis.CNC.auth.dto.LoginRequest;
import com.ingsis.CNC.auth.dto.RegisterRequest;
import com.ingsis.CNC.auth.dto.AuthResponse;
import com.ingsis.CNC.user.User;
import com.ingsis.CNC.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserRepository userRepository;
    @InjectMocks AuthService service;

    @Test
    void registerStoresHashedPasswordAndReturnsUserData() {
        UUID id = UUID.randomUUID();
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(id);
            return saved;
        });

        AuthResponse response = service.register(new RegisterRequest("ana", "ana@example.com", "secret"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertNotEquals("secret", captor.getValue().getPassword());
        assertTrue(captor.getValue().getPassword().startsWith("$2a$"));
        assertEquals(new AuthResponse(id, "ana", "ana@example.com"), response);
    }

    @Test
    void loginAcceptsCorrectPassword() {
        User user = new User("ana", "ana@example.com", new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("secret"));
        UUID id = UUID.randomUUID();
        user.setId(id);
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));

        assertEquals(new AuthResponse(id, "ana", "ana@example.com"), service.login(new LoginRequest("ana@example.com", "secret")));
    }

    @Test
    void loginRejectsWrongPasswordAndMissingUser() {
        User user = new User("ana", "ana@example.com", new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("secret"));
        when(userRepository.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
        assertThrows(RuntimeException.class, () -> service.login(new LoginRequest("ana@example.com", "wrong")));
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(java.util.NoSuchElementException.class, () -> service.login(new LoginRequest("missing@example.com", "secret")));
    }
}
