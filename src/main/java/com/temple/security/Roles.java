package com.temple.security;

import java.util.Locale;

public final class Roles {

    public static final String ADMIN = "ADMIN";

    private Roles() {
    }

    /** "admin", "ROLE_ADMIN", null → "ADMIN". Unknown values are kept (upper-cased) and get no admin rights. */
    public static String normalize(String role) {
        if (role == null || role.isBlank()) {
            return ADMIN;
        }
        String r = role.trim().toUpperCase(Locale.ROOT);
        return r.startsWith("ROLE_") ? r.substring(5) : r;
    }
}
