package com.temple.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.temple.config.AppProperties;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Stateless JWT security (same rules in every environment).
 * <ul>
 *   <li>Public: reading website content, event registration, newsletter (un)subscribe, login, health.</li>
 *   <li>Everything else — all create/update/delete, registrations, subscriber data, donor details — requires role ADMIN.</li>
 * </ul>
 * Paths are relative to the servlet context path ({@code /api}).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    static final String[] PUBLIC_GET = {
        "/events", "/events/*",
        "/services", "/services/*",
        "/staff", "/staff/*", "/staff/department/*",
        "/board-members", "/board-members/*", "/board-members/department/*",
        "/board-member-services/active", "/board-member-services/board-member/*",
        "/timings", "/timings/*", "/timings/day/*",
        "/donors", "/donors/statistics",
        "/contact", "/contact/active",
        "/bhojana-shala", "/bhojana-shala/*",
        "/goshala", "/goshala/*",
        "/vidyalaya", "/vidyalaya/*",
        "/sponsorships", "/sponsorships/*", "/sponsorships/purpose/*",
        "/photos/*/*",
        "/subscriptions/unsubscribe",
        "/actuator/health", "/actuator/health/**", "/actuator/info",
    };

    static final String[] PUBLIC_POST = {
        "/auth/login",
        "/events/register",
        "/subscriptions/subscribe",
        "/subscriptions/unsubscribe",
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ProblemJsonAuthHandlers authHandlers) throws Exception {
        http
            // Bearer-token API without cookies: CSRF protection is not applicable.
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            .headers(h -> h
                .contentTypeOptions(Customizer.withDefaults())
                .frameOptions(f -> f.deny())
                .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.GET, PUBLIC_GET).permitAll()
                .requestMatchers(HttpMethod.POST, PUBLIC_POST).permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.GET, "/auth/me").authenticated()
                .anyRequest().hasRole(Roles.ADMIN))
            .oauth2ResourceServer(o -> o
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .authenticationEntryPoint(authHandlers.entryPoint())
                .accessDeniedHandler(authHandlers.accessDeniedHandler()))
            .exceptionHandling(e -> e
                .authenticationEntryPoint(authHandlers.entryPoint())
                .accessDeniedHandler(authHandlers.accessDeniedHandler()));
        return http.build();
    }

    /**
     * Ignores the Authorization header on public endpoints, so a stale or
     * expired token held by a browser never breaks public pages with a 401.
     */
    @Bean
    BearerTokenResolver bearerTokenResolver() {
        List<RequestMatcher> matchers = new ArrayList<>();
        for (String pattern : PUBLIC_GET) {
            matchers.add(AntPathRequestMatcher.antMatcher(HttpMethod.GET, pattern));
        }
        for (String pattern : PUBLIC_POST) {
            matchers.add(AntPathRequestMatcher.antMatcher(HttpMethod.POST, pattern));
        }
        RequestMatcher publicEndpoints = new OrRequestMatcher(matchers);
        DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();
        return request -> publicEndpoints.matches(request) ? null : delegate.resolve(request);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        // bcrypt, with {id} prefixes so the algorithm can be upgraded later
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    SecretKey jwtSigningKey(AppProperties properties) {
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(properties.security().jwtSecret().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("JWT_SECRET must be base64 encoded (openssl rand -base64 48)", e);
        }
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET must decode to at least 32 bytes for HS256");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSigningKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSigningKey));
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSigningKey, AppProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSigningKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.security().issuer()));
        return decoder;
    }

    private static JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
