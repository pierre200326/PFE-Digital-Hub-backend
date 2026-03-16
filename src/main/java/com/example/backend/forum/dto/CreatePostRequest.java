package com.example.backend.forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotBlank(message = "Le contenu est obligatoire") @Size(max = 2000, message = "Le message est trop long") String content) {
}