package com.temple.security;

import java.time.Duration;

public class LoginBlockedException extends RuntimeException {

    private final Duration retryAfter;

    public LoginBlockedException(Duration retryAfter) {
        super("Too many failed login attempts");
        this.retryAfter = retryAfter;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }
}
