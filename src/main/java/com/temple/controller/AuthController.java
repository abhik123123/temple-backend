package com.temple.controller;

import com.temple.security.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    public record LoginRequest(
            @NotBlank @Size(max = 100) String username,
            @NotBlank @Size(max = 200) String password) {
    }

    public record CurrentUser(String username, String displayName, List<String> roles, Instant expiresAt) {
    }

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** POST /api/auth/login → { token, tokenType, expiresAt, username, displayName, roles } */
    @PostMapping("/login")
    public AuthService.LoginResult login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return authService.login(request.username(), request.password(), http.getRemoteAddr());
    }

    @GetMapping("/me")
    public CurrentUser me(@AuthenticationPrincipal Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        return new CurrentUser(jwt.getSubject(), jwt.getClaimAsString("name"),
                roles == null ? List.of() : roles, jwt.getExpiresAt());
    }
}
