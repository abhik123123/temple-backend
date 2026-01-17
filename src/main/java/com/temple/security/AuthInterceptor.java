package com.temple.security;

import com.temple.config.AuthConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Base64;

/**
 * Authentication Interceptor for environment-specific auth handling
 * - Local: Basic Auth (username:password)
 * - UAT/Prod: JWT Token Required
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

    @Autowired
    private AuthConfig authConfig;

    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin123";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String BASIC_PREFIX = "Basic ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        // Skip auth check for public endpoints
        if (isPublicEndpoint(requestPath, method)) {
            logger.debug("AUTH_SKIP | Public endpoint: {} {}", method, requestPath);
            return true;
        }

        // Skip auth check for login endpoint
        if (requestPath.contains("/api/auth/login") || requestPath.contains("/auth/login")) {
            logger.debug("AUTH_SKIP | Login endpoint: {} {}", method, requestPath);
            return true;
        }

        // Get authorization header
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // Local environment - allow basic auth (username:password)
        if (authConfig.isLocalEnvironment()) {
            logger.debug("AUTH_LOCAL | Checking basic auth for local environment");

            // If no auth header, allow in local for admin endpoints
            if (authHeader == null) {
                logger.debug("AUTH_LOCAL_ALLOW | No auth header required in local");
                return true;
            }

            // Check basic auth
            if (authHeader.startsWith(BASIC_PREFIX)) {
                return validateBasicAuth(authHeader);
            }

            // Allow JWT as well
            if (authHeader.startsWith(BEARER_PREFIX)) {
                logger.debug("AUTH_LOCAL_JWT | JWT token provided in local environment");
                return true; // In local, we allow JWT too
            }

            logger.debug("AUTH_LOCAL_ALLOW | Authorization header present");
            return true;
        }

        // UAT and Production - require JWT
        if (authConfig.isUATEnvironment() || authConfig.isProductionEnvironment()) {
            logger.debug("AUTH_SECURE | JWT required for {}", authConfig.getActiveProfile());

            if (authHeader == null) {
                logger.warn("AUTH_MISSING | No authorization header for {}", authConfig.getActiveProfile());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Authorization required. Please provide JWT token.\"}");
                return false;
            }

            if (!authHeader.startsWith(BEARER_PREFIX)) {
                logger.warn("AUTH_INVALID_FORMAT | Invalid authorization header format");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid authorization format. Use: Authorization: Bearer <token>\"}");
                return false;
            }

            logger.debug("AUTH_JWT_OK | Valid JWT header format");
            return true;
        }

        logger.debug("AUTH_ALLOW | Default allow");
        return true;
    }

    /**
     * Validate basic authentication (username:password)
     */
    private boolean validateBasicAuth(String authHeader) {
        try {
            String base64Credentials = authHeader.substring(BASIC_PREFIX.length());
            String credentials = new String(Base64.getDecoder().decode(base64Credentials));
            String[] parts = credentials.split(":");

            if (parts.length != 2) {
                logger.warn("AUTH_BASIC_INVALID | Invalid credentials format");
                return false;
            }

            String username = parts[0];
            String password = parts[1];

            // Check against default credentials
            if (DEFAULT_USERNAME.equals(username) && DEFAULT_PASSWORD.equals(password)) {
                logger.debug("AUTH_BASIC_SUCCESS | User authenticated: {}", username);
                return true;
            }

            logger.warn("AUTH_BASIC_FAILED | Invalid credentials");
            return false;

        } catch (Exception e) {
            logger.error("AUTH_BASIC_ERROR | Error validating basic auth: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if endpoint is public (no auth required)
     */
    private boolean isPublicEndpoint(String path, String method) {
        // Read operations are public
        if ("GET".equals(method)) {
            return path.contains("/api/events") ||
                    path.contains("/api/staff") ||
                    path.contains("/api/board-members") ||
                    path.contains("/api/photos") ||
                    path.contains("/events") ||
                    path.contains("/staff") ||
                    path.contains("/board-members");
        }

        return false;
    }
}

