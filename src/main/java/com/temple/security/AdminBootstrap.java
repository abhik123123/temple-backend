package com.temple.security;

import com.temple.config.AppProperties;
import com.temple.entity.Admin;
import com.temple.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the first administrator from ADMIN_USERNAME / ADMIN_PASSWORD when the
 * admins table is empty. Does nothing afterwards, so changing the variables
 * later never resets an existing password.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private static final int MIN_PASSWORD_LENGTH = 12;

    private final AdminRepository admins;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties.BootstrapAdmin config;

    public AdminBootstrap(AdminRepository admins, PasswordEncoder passwordEncoder, AppProperties properties) {
        this.admins = admins;
        this.passwordEncoder = passwordEncoder;
        this.config = properties.bootstrapAdmin();
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (admins.count() > 0) {
            return;
        }
        if (!config.isConfigured()) {
            log.warn("No admin accounts exist and ADMIN_USERNAME/ADMIN_PASSWORD are not set: nobody can log in.");
            return;
        }
        if (config.password().length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException("ADMIN_PASSWORD must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        admins.save(Admin.builder()
                .username(config.username().trim())
                .password(passwordEncoder.encode(config.password()))
                .name(config.displayName() == null || config.displayName().isBlank()
                        ? config.username() : config.displayName())
                .email(config.email() == null || config.email().isBlank() ? null : config.email())
                .role(Roles.ADMIN)
                .enabled(true)
                .build());
        log.info("Created initial administrator '{}'", config.username());
    }
}
