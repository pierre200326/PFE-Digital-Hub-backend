package com.example.backend.admin.dto;

import com.example.backend.user.Role;

public record UpdateUserRequest(
        String firstName,
        String lastName,
        String phone,
        Role role) {
}