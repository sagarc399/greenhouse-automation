package com.greenhouse.app.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

/**
 * Utility methods for reading, writing, and removing HTTP cookies.
 *
 * <p>Serialization uses plain Java object serialization encoded with Base64-URL,
 * which is sufficient for short-lived OAuth2 authorization-request cookies that
 * exist only for the duration of the Google login redirect round-trip.</p>
 */
public final class CookieUtils {

    private CookieUtils() { }

    /** Returns the named cookie from the request, or empty if absent. */
    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return Optional.empty();
        return Arrays.stream(cookies)
                .filter(c -> c.getName().equals(name))
                .findFirst();
    }

    /**
     * Writes a new cookie with {@code HttpOnly=true} and the given {@code maxAge}
     * (in seconds).  The path is always {@code /} so it is sent on every request
     * to the backend.
     */
    public static void addCookie(HttpServletResponse response,
                                  String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    /** Expires (deletes) a cookie by setting its {@code Max-Age} to 0. */
    public static void deleteCookie(HttpServletRequest request,
                                     HttpServletResponse response,
                                     String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return;
        Arrays.stream(cookies)
                .filter(c -> c.getName().equals(name))
                .forEach(c -> {
                    c.setValue("");
                    c.setPath("/");
                    c.setMaxAge(0);
                    response.addCookie(c);
                });
    }

    /** Serializes {@code object} to a Base64-URL string using Java serialization. */
    public static String serialize(Object object) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(object);
            return Base64.getUrlEncoder().encodeToString(bos.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize object to cookie value", e);
        }
    }

    /**
     * Deserializes a Base64-URL cookie value back to an instance of {@code cls}.
     *
     * @param cookie the cookie whose value to deserialize
     * @param cls    the expected type
     * @param <T>    the return type
     * @return the deserialized object, or {@code null} if the value is blank
     */
    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        String value = cookie.getValue();
        if (value == null || value.isBlank()) return null;
        try (ByteArrayInputStream bis =
                     new ByteArrayInputStream(Base64.getUrlDecoder().decode(value));
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return cls.cast(ois.readObject());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize cookie value", e);
        }
    }
}
