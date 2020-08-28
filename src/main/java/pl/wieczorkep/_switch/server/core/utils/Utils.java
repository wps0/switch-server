package pl.wieczorkep._switch.server.core.utils;

import lombok.extern.log4j.Log4j2;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Log4j2
public class Utils {
    private static final String AVAILABLE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789(,";

    private Utils() {}

    public static String generatePassword(int len) {
        SecureRandom sr;
        try {
            sr = SecureRandom.getInstance("PKCS11");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.info("PKCS11 library not installed, using default secure random number generator");
            LOGGER.trace(e);
            sr = new SecureRandom();
        }
        StringBuilder builder = new StringBuilder();
        while (len > 0) {
            builder.append(AVAILABLE_CHARS.charAt(sr.nextInt(AVAILABLE_CHARS.length())));
            len--;
        }
        return builder.toString();
    }

    public static String stripQuery(String query) {
        query = URLEncoder.encode(query, StandardCharsets.US_ASCII);
        int i = 0;
        StringBuilder builder = new StringBuilder();

        while (i < query.length()) {
            if (query.charAt(i) != '%') {
                builder.append(query.charAt(i));
                i++;
                continue;
            }

            int code = Integer.parseInt(query, i + 1, i + 3, 16);
            if (code == '-') {
                i += 3;
                builder.append("%2D");
                continue;
            }
            if (code < '0' || code > 'z' || code > '9' && code < 'A' || code > 'Z' && code < 'a') {
                i += 3;
            }
        }

        return builder.toString();
    }
}
