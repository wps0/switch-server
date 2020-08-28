package pl.wieczorkep._switch.server.core.utils;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void given0LenShouldReturnEmptyPassword() {
        // when
        String password = Utils.generatePassword(0);
        // then
        assertEquals("", password);
    }

    @Test
    void shouldGeneratePasswordUsingOnlyCharactersInGivenSet() {
        // given
        final int testAmount = 10;
        final int passwdLen = 48;
        final String availableChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789(,";
        boolean[] containsChar = new boolean[256];
        for (int i = 0; i < availableChars.length(); i++) {
            containsChar[availableChars.charAt(i)] = true;
        }
        // when
        for (int i = 0; i < testAmount; i++) {
            final String password = Utils.generatePassword(passwdLen);
            // then
            password.chars().forEach(c -> assertTrue(containsChar[c]));
        }
    }

    @Test
    void shouldGeneratePasswordsExactlyAsLongAsSpecified() {
        // given
        final int testAmount = 40;
        final int maxLen = 10240;
        Random r = new Random();
        // when
        for (int i = 0; i < testAmount; i++) {
            int len = r.nextInt(maxLen);
            final String password = Utils.generatePassword(len);
            // then
            assertEquals(len, password.length());
        }
    }
}