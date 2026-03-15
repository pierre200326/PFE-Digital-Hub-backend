package com.example.backend.admin;

import com.example.backend.admin.dto.AdminUserResponse;
import com.example.backend.admin.dto.UpdateUserRequest;
import com.example.backend.user.User;
import com.example.backend.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "Bienvenue admin";
    }

    @GetMapping("/users")
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    @GetMapping("/users/{id}")
    public AdminUserResponse getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        return AdminUserResponse.from(user);
    }

    @PutMapping("/users/{id}")
    public AdminUserResponse updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest req) {
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
        return AdminUserResponse.from(user);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable");
        }

        userRepository.deleteById(id);
    }
}