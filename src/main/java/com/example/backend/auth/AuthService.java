package com.example.backend.auth;

import com.example.backend.auth.dto.AuthResponse;
import com.example.backend.auth.dto.LoginRequest;
import com.example.backend.auth.dto.RegisterRequest;
import com.example.backend.user.User;
import com.example.backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest req) {
        if (req.phone() == null || req.phone().isBlank() ||
                req.password() == null || req.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing phone or password");
        }

        if (userRepository.existsByPhone(req.phone().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone already used");
        }

        User user = new User();
        user.setFirstName(req.firstName() == null ? "" : req.firstName().trim());
        user.setLastName(req.lastName() == null ? "" : req.lastName().trim());
        user.setPhone(req.phone().trim());
        user.setPasswordHash(passwordEncoder.encode(req.password()));

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest req) {
        if (req.phone() == null || req.phone().isBlank() ||
                req.password() == null || req.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing phone or password");
        }

        User user = userRepository.findByPhone(req.phone().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        }

        String token = jwtService.generateToken(user.getPhone());
        return new AuthResponse(token);
    }
}