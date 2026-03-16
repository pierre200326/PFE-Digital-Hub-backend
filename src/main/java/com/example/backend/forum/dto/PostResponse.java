package com.example.backend.forum.dto;

import com.example.backend.forum.Post;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String authorPhone,
        String content,
        LocalDateTime createdAt) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getAuthorPhone(),
                post.getContent(),
                post.getCreatedAt());
    }
}