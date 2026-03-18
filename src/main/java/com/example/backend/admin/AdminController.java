package com.example.backend.admin;

import com.example.backend.admin.dto.AdminUserResponse;
import com.example.backend.admin.dto.UpdateUserRequest;
import com.example.backend.audit.AuditLogRepository;
import com.example.backend.audit.AuditLogResponse;
import com.example.backend.audit.AuditLogService;
import com.example.backend.forum.Post;
import com.example.backend.forum.PostRepository;
import com.example.backend.forum.dto.PostResponse;
import com.example.backend.security.RequestUtils;
import com.example.backend.user.User;
import com.example.backend.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditLogService auditLogService;

    public AdminController(UserRepository userRepository,
            PostRepository postRepository,
            AuditLogRepository auditLogRepository,
            AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "Bienvenue admin";
    }

    @GetMapping("/audit-logs")
    public List<AuditLogResponse> getAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc()
                .stream()
                .map(AuditLogResponse::from)
                .toList();
    }

    @GetMapping("/users")
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    @GetMapping("/users/{id}")
    public AdminUserResponse getUserById(@PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        auditLogService.log(
                "ADMIN_GET_USER",
                authentication != null ? authentication.getName() : "unknown",
                RequestUtils.getClientIp(request),
                request.getMethod(),
                request.getRequestURI(),
                RequestUtils.getUserAgent(request),
                "SUCCESS",
                "Fetched user id=" + id);

        return AdminUserResponse.from(user);
    }

    @PutMapping("/users/{id}")
    public AdminUserResponse updateUser(@PathVariable Long id,
            @RequestBody UpdateUserRequest req,
            Authentication authentication,
            HttpServletRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        if (req.firstName() != null && !req.firstName().isBlank()) {
            user.setFirstName(req.firstName().trim());
        }
        if (req.lastName() != null && !req.lastName().isBlank()) {
            user.setLastName(req.lastName().trim());
        }
        if (req.phone() != null && !req.phone().isBlank()) {
            user.setPhone(req.phone().trim());
        }
        if (req.role() != null) {
            user.setRole(req.role());
        }

        userRepository.save(user);

        auditLogService.log(
                "ADMIN_UPDATE_USER",
                authentication != null ? authentication.getName() : "unknown",
                RequestUtils.getClientIp(request),
                request.getMethod(),
                request.getRequestURI(),
                RequestUtils.getUserAgent(request),
                "SUCCESS",
                "Updated user id=" + id);

        return AdminUserResponse.from(user);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable");
        }

        userRepository.deleteById(id);

        auditLogService.log(
                "ADMIN_DELETE_USER",
                authentication != null ? authentication.getName() : "unknown",
                RequestUtils.getClientIp(request),
                request.getMethod(),
                request.getRequestURI(),
                RequestUtils.getUserAgent(request),
                "SUCCESS",
                "Deleted user id=" + id);
    }

    @GetMapping("/forum")
    public List<PostResponse> getAllForumPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(PostResponse::from)
                .toList();
    }

    @DeleteMapping("/forum/{id}")
    public void deleteForumPost(@PathVariable Long id,
            Authentication authentication,
            HttpServletRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message introuvable"));

        postRepository.delete(post);

        auditLogService.log(
                "ADMIN_DELETE_FORUM_POST",
                authentication != null ? authentication.getName() : "unknown",
                RequestUtils.getClientIp(request),
                request.getMethod(),
                request.getRequestURI(),
                RequestUtils.getUserAgent(request),
                "SUCCESS",
                "Deleted forum post id=" + id);
    }
}