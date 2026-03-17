package com.example.backend.forum;

import com.example.backend.forum.dto.CreatePostRequest;
import com.example.backend.forum.dto.PostResponse;
import com.example.backend.security.AuditLogService;
import com.example.backend.security.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/forum")
public class PostController {

    private final PostRepository postRepository;
    private final AuditLogService auditLogService;

    public PostController(PostRepository postRepository, AuditLogService auditLogService) {
        this.postRepository = postRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<PostResponse> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(PostResponse::from)
                .toList();
    }

    @GetMapping("/admin/forum")
    public List<PostResponse> getAllPostsForAdmin() {
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(PostResponse::from)
                .toList();
    }

    @PostMapping
    public PostResponse createPost(@Valid @RequestBody CreatePostRequest req,
            Authentication authentication,
            HttpServletRequest request) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vous devez être connecté");
        }

        Post post = new Post();
        post.setAuthorPhone(authentication.getName());
        post.setContent(req.content().trim());
        post.setCreatedAt(LocalDateTime.now());

        postRepository.save(post);

        auditLogService.log(
                "FORUM_POST_CREATE",
                authentication.getName(),
                RequestUtils.getClientIp(request),
                request.getMethod(),
                request.getRequestURI(),
                RequestUtils.getUserAgent(request),
                "SUCCESS",
                "Created forum post with id=" + post.getId()
        );

        return PostResponse.from(post);
    }
}