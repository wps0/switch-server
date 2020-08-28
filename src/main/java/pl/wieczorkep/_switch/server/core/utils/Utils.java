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
        return stripQuery(query, false);
    }

    public static String stripQuery(String query, boolean aggressive) {
        String encoded = URLEncoder.encode(query, StandardCharsets.US_ASCII).replaceAll("%..", "");
        if (aggressive) {
            return encoded.replace("*", "")
                    .replace("_", "")
                    .replace(".", "");
        }
        return encoded;
    }
}
