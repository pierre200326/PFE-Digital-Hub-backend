package com.example.backend.forum;

import com.example.backend.forum.dto.CreatePostRequest;
import com.example.backend.forum.dto.PostResponse;
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

    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
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
            Authentication authentication) {

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

        return PostResponse.from(post);
    }
}