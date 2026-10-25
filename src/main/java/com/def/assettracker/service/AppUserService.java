
package com.def.assettracker.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.def.assettracker.entity.AppUser;
import com.def.assettracker.entity.Role;
import com.def.assettracker.repository.AppUserRepository;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AppUser> getAll() {
        return appUserRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AppUser getById(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional
    public AppUser create(AppUser user) {
        if (user.getFullName() == null
                || user.getFullName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Full name is required");
        }

        if (user.getEmail() == null
                || user.getEmail().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email is required");
        }

        if (user.getPassword() == null
                || user.getPassword().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password is required");
        }

        String email = user.getEmail().trim();

        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists");
        }

        user.setEmail(email);
        user.setPassword(
                passwordEncoder.encode(user.getPassword()));

        // Public registration always creates a STAFF account.
        user.setRole(Role.STAFF);

        return appUserRepository.save(user);
    }
}
