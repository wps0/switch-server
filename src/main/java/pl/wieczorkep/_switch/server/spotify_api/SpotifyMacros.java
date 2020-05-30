package pl.wieczorkep._switch.server.spotify_api;

import com.google.gson.Gson;
import pl.wieczorkep._switch.server.spotify_api.model.CurrentlyPlaying;
import pl.wieczorkep._switch.server.spotify_api.model.Devices;

import java.net.URI;

import static pl.wieczorkep._switch.server.spotify_api.SpotifyApiGateway.AuthMethod.BEARER;
import static pl.wieczorkep._switch.server.spotify_api.SpotifyApiGateway.RequestMethod.GET;

public class SpotifyMacros {
    private static Gson gson = new Gson();

    public static class Player {
        public static String API_PREFIX = SpotifyApiGateway.API_ENDPOINT + "/me/player";
        private Player() {}

        /**
         * @see <a href="https://developer.spotify.com/documentation/web-api/reference/player/get-information-about-the-users-current-playback/">Spotify API reference</a>
         */
        public static CurrentlyPlaying getPlaybackInfo(SpotifyApiGateway apiGateway) {
            return parse(apiGateway.makeRequest(URI.create(API_PREFIX),
                    "",
                    GET,
                    BEARER).body(), CurrentlyPlaying.class);
        }

        /**
         * @see <a href="https://developer.spotify.com/documentation/web-api/reference/player/get-a-users-available-devices/">Spotify API reference</a>
         */
        public static Devices getAvailableDevices(SpotifyApiGateway apiGateway) {
            return parse(apiGateway.makeRequest(URI.create(API_PREFIX + "/devices"),
                    "",
                    GET,
                    BEARER).body(), Devices.class);
        }
    }

    private static <T> T parse(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }
}
