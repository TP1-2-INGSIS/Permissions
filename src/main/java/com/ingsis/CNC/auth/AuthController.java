package com.ingsis.CNC.auth;

import com.ingsis.CNC.auth.dto.LoginRequest;
import com.ingsis.CNC.auth.dto.RegisterRequest;
import com.ingsis.CNC.auth.dto.AuthResponse;
import com.ingsis.CNC.user.User;
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
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request){
        return ResponseEntity.ok(service.login(request));
    }

}
