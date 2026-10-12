package com.ingsis.CNC.auth;

import com.ingsis.CNC.auth.dto.LoginRequest;
import com.ingsis.CNC.auth.dto.RegisterRequest;
import com.ingsis.CNC.auth.dto.AuthResponse;
import com.ingsis.CNC.user.UserRepository;
import com.ingsis.CNC.user.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthResponse register(RegisterRequest request) {
        String hashedPassword = encoder.encode(request.password());
        User user = new User(request.userName(), request.email(), hashedPassword);
        User saved = userRepository.save(user);
        return new AuthResponse(saved.getId(), saved.getUserName(), saved.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email()).orElseThrow();
        if (!encoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        return new AuthResponse(user.getId(), user.getUserName(), user.getEmail());
    }

}
