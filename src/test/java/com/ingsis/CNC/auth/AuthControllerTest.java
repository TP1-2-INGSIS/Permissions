package com.ingsis.CNC.auth;

import com.ingsis.CNC.auth.dto.AuthResponse;
import com.ingsis.CNC.auth.dto.LoginRequest;
import com.ingsis.CNC.auth.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {
    private final AuthService service = mock(AuthService.class);
    private final AuthController controller = new AuthController(service);

    @Test
    void delegatesRegistrationAndLogin() {
        AuthResponse response = new AuthResponse(UUID.randomUUID(), "ana", "ana@example.com");
        RegisterRequest register = new RegisterRequest("ana", "ana@example.com", "secret");
        LoginRequest login = new LoginRequest("ana@example.com", "secret");
        when(service.register(register)).thenReturn(response);
        when(service.login(login)).thenReturn(response);

        assertEquals(HttpStatus.OK, controller.register(register).getStatusCode());
        assertEquals(response, controller.register(register).getBody());
        assertEquals(HttpStatus.OK, controller.login(login).getStatusCode());
        assertEquals(response, controller.login(login).getBody());
    }
}
