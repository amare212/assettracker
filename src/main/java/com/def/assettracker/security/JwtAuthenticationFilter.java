
package com.def.assettracker.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.def.assettracker.entity.AppUser;
import com.def.assettracker.repository.AppUserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            AppUserRepository appUserRepository) {

        this.jwtService = jwtService;
        this.appUserRepository = appUserRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authorization =
                request.getHeader("Authorization");

        // No token: continue. SecurityConfig decides
        // whether the endpoint requires authentication.
        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7).trim();

        try {
            if (token.isEmpty()
                    || !jwtService.isTokenValid(token)) {

                rejectRequest(response);
                return;
            }

            String email = jwtService.extractEmail(token);

            AppUser user = appUserRepository
                    .findByEmailIgnoreCase(email)
                    .orElse(null);

            if (user == null || user.getRole() == null) {
                rejectRequest(response);
                return;
            }

            // Load the current role from the database.
            String authority =
                    "ROLE_" + user.getRole().name();

            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            null,
                            List.of(
                                    new SimpleGrantedAuthority(
                                            authority)));

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (RuntimeException exception) {
            // Invalid, malformed, or expired token.
            SecurityContextHolder.clearContext();
            rejectRequest(response);
        }
    }

    private void rejectRequest(
            HttpServletResponse response)
            throws IOException {

        SecurityContextHolder.clearContext();

        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Invalid or expired bearer token");
    }
}
