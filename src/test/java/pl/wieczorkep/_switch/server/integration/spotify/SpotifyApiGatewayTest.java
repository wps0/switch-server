package pl.wieczorkep._switch.server.integration.spotify;

import org.junit.jupiter.api.Test;
import pl.wieczorkep._switch.server.Constants;
import pl.wieczorkep._switch.server.core.AppConfig;
import pl.wieczorkep._switch.server.integration.spotify.SpotifyApiGateway.AuthMethod;
import pl.wieczorkep._switch.server.integration.spotify.SpotifyApiGateway.GrantType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SpotifyApiGatewayTest {

    static class GrantTypeTest {
        @Test
        void shouldReturnValidAuthTypeStrings() {
            // then
            assertEquals(GrantType.AUTH_CODE.toString(), "authorization_code");
            assertEquals(GrantType.REFRESH.toString(), "refresh_token");
        }
    }

    static class AuthMethodTest {
        @Test
        void shouldReturnValidBasicAuthString() {
            // given
            AuthMethod authMethod = AuthMethod.BASIC;
            AppConfig appConfig = mock(AppConfig.class);
            String expected = "Basic MTIzNDU2Nzg5MHF3ZXJ0eXVpb3A6c2VjcmV0MTIz";
            // when
            when(appConfig.get(Constants.ACTION_SPOTIFY_APPID)).thenReturn("1234567890qwertyuiop");
            when(appConfig.get(Constants.ACTION_SPOTIFY_APPSECRET)).thenReturn("secret123");
            String actual = authMethod.authString(appConfig);
            // then
            assertEquals(expected, actual);
        }

        @Test
        void shouldReturnValidBearerAuthString() {
            // given
            AuthMethod authMethod = AuthMethod.BEARER;
            AppConfig appConfig = mock(AppConfig.class);
            String expected = "Bearer oVqXUh0E5u5YLFrgKU8Vq2c12zlKRvg";
            // when
            when(appConfig.get(Constants.ACTION_SPOTIFY_CLIENT_TOKEN)).thenReturn("oVqXUh0E5u5YLFrgKU8Vq2c12zlKRvg");
            String actual = authMethod.authString(appConfig);
            // then
            assertEquals(expected, actual);
        }
    }
}
