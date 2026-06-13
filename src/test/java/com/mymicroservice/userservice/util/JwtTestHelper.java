package com.mymicroservice.userservice.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public final class JwtTestHelper {

    private JwtTestHelper() {
    }

    public static String createBearerToken(String subject, List<String> roles) {
        String rolesJson = roles.isEmpty()
                ? "[]"
                : "[\"" + String.join("\",\"", roles) + "\"]";
        String payloadJson = "{\"sub\":\"" + subject + "\",\"roles\":" + rolesJson + "}";
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        return "Bearer header." + encodedPayload + ".signature";
    }

    public static String createInvalidBearerToken() {
        return "Bearer invalid-token";
    }
}
