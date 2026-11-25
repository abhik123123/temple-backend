package com.temple.security;

import com.temple.entity.Admin;
import com.temple.repository.AdminRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public record LoginResult(String token, String tokenType, Instant expiresAt,
                              String username, String displayName, List<String> roles) {
    }

    private final AdminRepository admins;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService attempts;
    /** Checked when the username doesn't exist so timing doesn't reveal valid usernames. */
    private final String dummyHash;

    public AuthService(AdminRepository admins, PasswordEncoder passwordEncoder,
                       JwtService jwtService, LoginAttemptService attempts) {
        this.admins = admins;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.attempts = attempts;
        this.dummyHash = passwordEncoder.encode("timing-equaliser-not-a-real-password");
    }

    @Transactional
    public LoginResult login(String username, String password, String clientIp) {
        attempts.checkAllowed(username, clientIp);

        Admin admin = admins.findByUsernameIgnoreCase(username.trim()).orElse(null);
        boolean valid = admin != null && admin.isEnabled() && passwordMatches(admin, password);
        if (admin == null) {
            passwordEncoder.matches(password, dummyHash);
        }
        if (!valid) {
            attempts.recordFailure(username, clientIp);
            log.info("LOGIN_FAILED | username={} | ip={}", sanitize(username), clientIp);
            throw new BadCredentialsException("Invalid username or password");
        }

        attempts.recordSuccess(username, clientIp);
        JwtService.IssuedToken token = jwtService.issue(admin);
        log.info("LOGIN_SUCCESS | username={}", admin.getUsername());
        return new LoginResult(token.value(), "Bearer", token.expiresAt(), admin.getUsername(),
                admin.getName() == null ? admin.getUsername() : admin.getName(),
                List.of(Roles.normalize(admin.getRole())));
    }

    /**
     * Accepts bcrypt (or any {id}-prefixed) hashes. Rows created before this
     * release stored plaintext; those are compared in constant time once and
     * immediately re-hashed, so no plaintext survives the first login.
     */
    private boolean passwordMatches(Admin admin, String rawPassword) {
        String stored = admin.getPassword();
        if (stored == null) {
            return false;
        }
        if (stored.startsWith("{")) {
            return passwordEncoder.matches(rawPassword, stored);
        }
        boolean legacyMatch = MessageDigest.isEqual(
                stored.getBytes(StandardCharsets.UTF_8), rawPassword.getBytes(StandardCharsets.UTF_8));
        if (legacyMatch) {
            admin.setPassword(passwordEncoder.encode(rawPassword));
            admins.save(admin);
            log.warn("LEGACY_PASSWORD_UPGRADED | username={} | plaintext password replaced with bcrypt hash",
                    admin.getUsername());
        }
        return legacyMatch;
    }

    /** Prevents log forging via CR/LF in user-supplied usernames. */
    private static String sanitize(String value) {
        String s = value == null ? "" : value.replaceAll("[\\r\\n\\t]", "_");
        return s.length() > 100 ? s.substring(0, 100) : s;
    }
}
