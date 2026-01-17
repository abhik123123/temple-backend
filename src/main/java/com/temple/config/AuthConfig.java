package com.temple.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Configuration for environment-specific authentication
 */
@Component
public class AuthConfig {

    @Value("${app.auth.enabled:true}")
    private boolean jwtEnabled;

    @Value("${app.auth.basic-enabled:false}")
    private boolean basicAuthEnabled;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    public boolean isJwtEnabled() {
        return jwtEnabled;
    }

    public boolean isBasicAuthEnabled() {
        return basicAuthEnabled;
    }

    public String getActiveProfile() {
        return activeProfile;
    }

    public boolean isLocalEnvironment() {
        return "local".equalsIgnoreCase(activeProfile);
    }

    public boolean isUATEnvironment() {
        return "uat".equalsIgnoreCase(activeProfile);
    }

    public boolean isProductionEnvironment() {
        return "prod".equalsIgnoreCase(activeProfile);
    }
}

