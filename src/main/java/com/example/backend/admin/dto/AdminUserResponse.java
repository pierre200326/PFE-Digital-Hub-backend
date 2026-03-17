package com.example.backend.admin.dto;

import com.example.backend.user.Role;
import com.example.backend.user.User;

public record AdminUserResponse(
        Long id,
        String firstName,
        String lastName,
        String phone,
        Role role) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getRole());
    }
}