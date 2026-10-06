package com.ingsis.CNC.auth;

import com.ingsis.CNC.auth.dto.RegisterRequest;
import com.ingsis.CNC.auth.dto.RegisterResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> searchUsers(@RequestParam(value = "query", defaultValue = "") String query) {
        return ResponseEntity.ok(service.searchUsers(query));
    }
}
