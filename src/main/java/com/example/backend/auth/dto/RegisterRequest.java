package com.example.backend.auth.dto;

public record RegisterRequest(
        String firstName,
        String lastName,
        String phone,
        String password) {
}