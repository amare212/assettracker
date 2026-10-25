package com.def.assettracker.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.def.assettracker.entity.AppUser;
import com.def.assettracker.entity.Role;
import com.def.assettracker.repository.AppUserRepository;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserController(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder) {

        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ADMIN: Retrieve all users
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        List<UserResponse> users = appUserRepository.findAll()
                .stream()
                .map(this::toUserResponse)
                .toList();

        return ResponseEntity.ok(users);
    }

    // ADMIN: Retrieve one user by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {

        return appUserRepository.findById(id)
                .<ResponseEntity<?>>map(user ->
                        ResponseEntity.ok(toUserResponse(user)))
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of(
                                        "message", "User not found")));
    }

    /*
     * Public registration:
     * - Always creates STAFF.
     * - Rejects role selection.
     *
     * Authenticated ADMIN:
     * - Can create STAFF or ADMIN.
     */
    @PostMapping
    public ResponseEntity<?> createUser(
            @RequestBody Map<String, String> request) {

        String fullName = request.get("fullName");
        String email = request.get("email");
        String password = request.get("password");
        String requestedRole = request.get("role");

        // Validate required fields
        if (fullName == null || fullName.isBlank()
                || email == null || email.isBlank()
                || password == null || password.isBlank()) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "fullName, email and password are required"));
        }

        // Validate password length
        if (password.length() < 8) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "message",
                            "Password must be at least 8 characters long"));
        }

        String normalizedEmail = email.trim();

        // Check for duplicate email
        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("message", "Email already exists"));
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_ADMIN")
                                || authority.getAuthority().equals("ADMIN"));

        Role role = Role.STAFF;

        // A public request cannot choose its role.
        if (requestedRole != null && !requestedRole.isBlank()) {

            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        Map.of(
                                "message",
                                "Only an ADMIN can assign a user role"));
            }

            try {
                role = Role.valueOf(
                        requestedRole.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "message",
                                "Invalid role. Use ADMIN or STAFF"));
            }
        }

        // Create the account
        AppUser user = new AppUser();
        user.setFullName(fullName.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        AppUser savedUser = appUserRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "id", savedUser.getId(),
                        "fullName", savedUser.getFullName(),
                        "email", savedUser.getEmail(),
                        "role", savedUser.getRole().name(),
                        "createdAt", savedUser.getCreatedAt(),
                        "message", "User created successfully"));
    }

    // Never return the password in API responses.
    private UserResponse toUserResponse(AppUser user) {

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt());
    }

    public record UserResponse(
            Long id,
            String fullName,
            String email,
            Role role,
            LocalDateTime createdAt) {
    }
}