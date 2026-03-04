package com.example.backend.auth.dto;

public record LoginRequest(
        String phone,
        String password) {
}