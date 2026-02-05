package com.payrix.administrator.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;

public final class TokenUtil {

    private TokenUtil() {}

    public static String getAccessToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
