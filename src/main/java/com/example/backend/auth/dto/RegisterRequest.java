package com.example.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Le prénom est requis")
        @Size(min = 2, max = 50, message = "Le prénom doit contenir entre 2 caractères minimum et 50 caractères maximum")
        String firstName,

        @NotBlank(message = "Le nom de famille est requis")
        @Size(min = 2, max = 50, message = "Le nom de famille doit contenir entre 2 caractères minimum et 50 caractères maximum")
        String lastName,

        @NotBlank(message = "Le numéro de téléphone est requis")
        @Pattern(regexp = "^[0-9]{10}$", message = "Le numéro de téléphone doit contenir 10 chiffres")
        String phone,

        @NotBlank(message = "Le mot de passe est requis")
        @Size(min = 10, message = "Le mot de passe doit contenir au moins 10 caractères")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Le mot de passe doit contenir au moins une lettre majuscule, une lettre minuscule et un chiffre"
        )
        String password

) {}