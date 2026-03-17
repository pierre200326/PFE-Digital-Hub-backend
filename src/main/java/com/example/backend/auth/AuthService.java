package com.example.backend.auth;

import com.example.backend.auth.dto.AuthResponse;
import com.example.backend.auth.dto.LoginRequest;
import com.example.backend.auth.dto.RegisterRequest;
import com.example.backend.security.AuditLogService;
import com.example.backend.security.RequestUtils;
import com.example.backend.user.Role;
import com.example.backend.user.User;
import com.example.backend.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditLogService = auditLogService;
    }

    public void register(RegisterRequest req) {
        if (req.phone() == null || req.phone().isBlank() || req.password() == null || req.password().isBlank()) {
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
        user.setRole(Role.USER);

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest req, HttpServletRequest request) {
        String clientIp = RequestUtils.getClientIp(request);
        String method = request.getMethod();
        String path = request.getRequestURI();
        String userAgent = RequestUtils.getUserAgent(request);

        if (req.phone() == null || req.phone().isBlank() || req.password() == null || req.password().isBlank()) {
            auditLogService.log(
                    "LOGIN_FAILED",
                    req.phone(),
                    clientIp,
                    method,
                    path,
                    userAgent,
                    "FAILED",
                    "Missing phone or password"
            );
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing phone or password");
        }

        User user = userRepository.findByPhone(req.phone().trim())
                .orElseThrow(() -> {
                    auditLogService.log(
                            "LOGIN_FAILED",
                            req.phone(),
                            clientIp,
                            method,
                            path,
                            userAgent,
                            "FAILED",
                            "Unknown phone"
                    );
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
                });

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            auditLogService.log(
                    "LOGIN_FAILED",
                    user.getPhone(),
                    clientIp,
                    method,
                    path,
                    userAgent,
                    "FAILED",
                    "Bad password"
            );
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        }

        String token = jwtService.generateToken(user.getPhone(), user.getRole().name());

        auditLogService.log(
                "LOGIN_SUCCESS",
                user.getPhone(),
                clientIp,
                method,
                path,
                userAgent,
                "SUCCESS",
                "User authenticated successfully"
        );

        return new AuthResponse(token);
    }
}