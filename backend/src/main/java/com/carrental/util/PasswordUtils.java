package com.carrental.util;

import java.nio.charset.StandardCharsets;
import org.springframework.util.DigestUtils;

public final class PasswordUtils {
    private static final String SALT = "car-rental-demo";

    private PasswordUtils() {
    }

    public static String encode(String rawPassword) {
        return DigestUtils.md5DigestAsHex((rawPassword + SALT).getBytes(StandardCharsets.UTF_8));
    }
}
