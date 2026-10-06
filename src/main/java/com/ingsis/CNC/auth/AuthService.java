package com.ingsis.CNC.auth;

import com.ingsis.CNC.auth.dto.RegisterRequest;
import com.ingsis.CNC.auth.dto.RegisterResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
    private final AuthRepository authRepository;

    public AuthService(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public RegisterResponse register(RegisterRequest request) {
        User user = new User(request.getUserName(), request.getEmail(), request.getPassword());
        User saved = authRepository.save(user);
        return new RegisterResponse(saved.getId(), saved.getUserName(), saved.getEmail());
    }

    public Optional<User> findById(UUID id) {
        return authRepository.findById(id);
    }

    public List<User> searchUsers(String query) {
        return authRepository.findByUserNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }
}
