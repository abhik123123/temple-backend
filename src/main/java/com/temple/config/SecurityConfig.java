package com.temple.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(AppAuthProperties.class)
public class SecurityConfig {
    
    private final AppAuthProperties authProperties;
    
    public SecurityConfig(AppAuthProperties authProperties) {
        this.authProperties = authProperties;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if (!authProperties.isEnabled()) {
            // Local environment: no authentication required
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());
        } else {
            // UAT/Production: authentication required
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/h2-console/**").denyAll()
                    .anyRequest().authenticated()
                )
                .httpBasic(basic -> {})
                .formLogin(form -> form.disable());
        }
        
        return http.build();
    }
}

@ConfigurationProperties(prefix = "app.auth")
class AppAuthProperties {
    private boolean enabled = false;
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
