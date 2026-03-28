package com.administrativetool.controller.api;

import com.administrativetool.domain.dto.UserResponse;
import com.administrativetool.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final SecurityContextRepository securityContextRepository =
        new HttpSessionSecurityContextRepository();

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
        final @RequestBody LoginRequest request,
        final HttpServletRequest httpRequest,
        final HttpServletResponse httpResponse
    ) {
        try {
            final var authToken = new UsernamePasswordAuthenticationToken(
                request.username(), request.password()
            );
            final var authentication = authenticationManager.authenticate(authToken);

            final var securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            securityContextRepository.saveContext(securityContext, httpRequest, httpResponse);

            final var user = userService.findByUsername(request.username())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

            return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "user", UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .role(user.getRole())
                    .build()
            ));
        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for user: {}", request.username());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid username or password"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(final Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Not authenticated"));
        }

        final var user = userService.findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalStateException("User not found: " + authentication.getName()));

        return ResponseEntity.ok(Map.of(
            "user", UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build()
        ));
    }

    public record LoginRequest(String username, String password) {
    }
}
