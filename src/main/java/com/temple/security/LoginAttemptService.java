package com.temple.security;

import com.temple.config.AppProperties;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Brute-force protection for the login endpoint: after N failures for the same
 * username (or the same client IP) further attempts are rejected for a lockout
 * window. In-memory, which is adequate for a single instance; move to Redis if
 * the backend is ever scaled horizontally.
 */
@Service
public class LoginAttemptService {

    private record Attempts(int failures, Instant windowStart) {
    }

    private static final int IP_LIMIT_MULTIPLIER = 4;

    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final Duration lockout;
    private final Clock clock;

    @Autowired
    public LoginAttemptService(AppProperties properties) {
        this(properties, Clock.systemUTC());
    }

    LoginAttemptService(AppProperties properties, Clock clock) {
        this.maxAttempts = properties.security().loginMaxAttempts();
        this.lockout = properties.security().loginLockout();
        this.clock = clock;
    }

    public void checkAllowed(String username, String clientIp) {
        for (String key : keys(username, clientIp)) {
            Attempts a = current(key);
            int limit = key.startsWith("ip:") ? maxAttempts * IP_LIMIT_MULTIPLIER : maxAttempts;
            if (a != null && a.failures() >= limit) {
                Duration remaining = Duration.between(clock.instant(), a.windowStart().plus(lockout));
                throw new LoginBlockedException(remaining.isNegative() ? Duration.ZERO : remaining);
            }
        }
    }

    public void recordFailure(String username, String clientIp) {
        Instant now = clock.instant();
        for (String key : keys(username, clientIp)) {
            attempts.compute(key, (k, a) -> (a == null || expired(a, now))
                    ? new Attempts(1, now)
                    : new Attempts(a.failures() + 1, a.windowStart()));
        }
    }

    public void recordSuccess(String username, String clientIp) {
        attempts.remove(userKey(username));
    }

    @Scheduled(fixedDelay = 600_000)
    void evictExpired() {
        Instant now = clock.instant();
        attempts.entrySet().removeIf(e -> expired(e.getValue(), now));
    }

    private Attempts current(String key) {
        Attempts a = attempts.get(key);
        if (a != null && expired(a, clock.instant())) {
            attempts.remove(key, a);
            return null;
        }
        return a;
    }

    private boolean expired(Attempts a, Instant now) {
        return a.windowStart().plus(lockout).isBefore(now);
    }

    private static String[] keys(String username, String clientIp) {
        // IP limit is looser so a shared office/NAT address isn't locked out by one user.
        return new String[] {userKey(username), "ip:" + clientIp};
    }

    private static String userKey(String username) {
        return "user:" + (username == null ? "" : username.trim().toLowerCase(Locale.ROOT));
    }
}
