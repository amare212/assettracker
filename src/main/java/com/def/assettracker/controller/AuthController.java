package com.def.assettracker.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.def.assettracker.entity.AppUser;
import com.def.assettracker.repository.AppUserRepository;
import com.def.assettracker.security.JwtService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request.email() == null || request.email().isBlank()
                || request.password() == null || request.password().isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Email and password are required"));
        }

        AppUser user = appUserRepository.findByEmailIgnoreCase(
                        request.email().trim())
                .orElse(null);
        if (user == null || !passwordEncoder.matches(
                request.password(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Invalid email or password"));
        }

        return ResponseEntity.ok(Map.of(
                "token", jwtService.generateToken(user),
                "tokenType", "Bearer"
        ));
    }

    private record LoginRequest(String email, String password) {
    }
}
