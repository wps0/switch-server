package pl.wieczorkep._switch.server.integration.spotify;

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import pl.wieczorkep._switch.server.integration.spotify.model.CurrentlyPlaying;
import pl.wieczorkep._switch.server.integration.spotify.model.Devices;

import java.net.URI;
import java.net.http.HttpResponse;

import static pl.wieczorkep._switch.server.Constants.CONST_SPOTIFY_API_ENDPOINT;
import static pl.wieczorkep._switch.server.integration.spotify.SpotifyApiGateway.AuthMethod.BEARER;
import static pl.wieczorkep._switch.server.integration.spotify.SpotifyApiGateway.RequestMethod.*;

@Log4j2
public class SpotifyMacros {
    private static final Gson gson = new Gson();

    private SpotifyMacros() {}

    /**
     * Player endpoint.
     */
    public static class Player {
        public static final String API_PREFIX = CONST_SPOTIFY_API_ENDPOINT + "/me/player";

        private Player() {}

        /**
         * @param apiGateway Spotify API gateway object.
         * @see <a href="https://developer.spotify.com/documentation/web-api/reference/player/get-information-about-the-users-current-playback/">Spotify API reference</a>
         */
        public static CurrentlyPlaying getPlaybackInfo(SpotifyApiGateway apiGateway) {
            return decode(apiGateway.makeRequest(URI.create(API_PREFIX),
                    "",
                    "", GET,
                    BEARER).body(), CurrentlyPlaying.class);
        }

        /**
         * @param apiGateway Spotify API gateway object.
         * @see <a href="https://developer.spotify.com/documentation/web-api/reference/player/get-a-users-available-devices/">Spotify API reference</a>
         */
        public static Devices getAvailableDevices(SpotifyApiGateway apiGateway) {
            return decode(apiGateway.makeRequest(URI.create(API_PREFIX + "/devices"),
                    "",
                    "", GET,
                    BEARER).body(), Devices.class);
        }

        /**
         * Pause a user's playback
         *
         * @param apiGateway Spotify API gateway object.
         * @param deviceId   Optional. The id of the device this command is targeting. If not supplied, the user’s
         *                   currently active device is the target.
         * @return True if the status code is equals to 204, false otherwise.
         */
        public static HttpResponse<String> pausePlayback(SpotifyApiGateway apiGateway, String deviceId) {
            return apiGateway.makeRequest(URI.create(API_PREFIX + "/pause"), deviceId, "", PUT, BEARER);
        }

        /**
         * @param apiGateway Spotify API gateway object.
         * @param deviceId   Optional. The id of the device this command is targeting. If not supplied, the user’s currently
         *                   active device is the target.
         * @param contextUri Optional. Spotify URI of the context to play. Valid contexts are albums, artists, playlists.
         *                   Example: "spotify:album:1Je1IMUlBXcx1Fz0WE7oPT"
         * @param offset     Optional. Indicates from where in the context playback should start. Only available when context_uri
         *                   corresponds to an album or playlist object, or when the uris parameter is used. “position”
         *                   is zero based and can’t be negative. Example: "offset": {"position": 5}
         * @param positionMs Optional. Indicates from what position to start playback. Must be a positive number. Passing
         *                   in a position that is greater than the length of the track will cause the player to start
         *                   playing the next song.
         * @return True if the status code is equals to 204, false otherwise
         */
        public static HttpResponse<String> startPlayback(SpotifyApiGateway apiGateway, @NotNull String deviceId, @NotNull String contextUri,
                                            int offset, int positionMs) {
            if (positionMs < 0) {
                positionMs = 0;
            }

            StringBuilder headerArguments = new StringBuilder();
            if (!deviceId.isBlank()) {
                headerArguments.append("device_id=").append(deviceId);
            }

            StringBuilder bodyArguments = new StringBuilder();
            if (!contextUri.isBlank()) {
                bodyArguments.append("context_uri=").append(contextUri).append('&');
            }

            bodyArguments.append("offset=").append("position=").append(offset).append("&");
            bodyArguments.append("position_ms=").append(positionMs);

            LOGGER.debug(encode(bodyArguments.toString()));

            return apiGateway.makeRequest(URI.create(API_PREFIX + "/play"), headerArguments.toString(), encode(bodyArguments.toString()), PUT, BEARER);
        }

        /**
         * Change user's volume
         *
         * @param apiGateway Spotify API gateway object.
         * @param deviceId   Optional. The id of the device this command is targeting. If not supplied, the user’s
         *                   currently active device is the target.
         * @param volume     Volume in range 0-100. If volume is out of range, it's rounded to 100 (if volume > 100)
         *                   or 0 (if volume < 0).
         * @return True if the status code is equals to 204, false otherwise.
         */
        public static HttpResponse<String> changeVolume(SpotifyApiGateway apiGateway, String deviceId, int volume) {
            if (volume > 100) {
                volume = 100;
            } else if (volume < 0) {
                volume = 0;
            }

            StringBuilder headerArguments = new StringBuilder();
            headerArguments.append("volume_percent=").append(volume);

            if (!deviceId.isBlank()) {
                headerArguments.append("&device_id=").append(deviceId);
            }

            return apiGateway.makeRequest(URI.create(API_PREFIX + "/volume"), headerArguments.toString(),
                    "", PUT, BEARER);
        }
    }

    public static <T> T decode(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }

    /**
     * @param args Key-Value pairs, eg. offset=alamakocurr=a0&position_ms=0
     */
    public static String encode(@NotNull String args) {
        final String[] arguments = args.split("&");
        if (arguments.length <= 1) {
            if (arguments[0].equals("")) {
                return "{}";
            }
            if (args.split("=").length <= 1) {
                return "\"" + args + "\"";
            }
        }

        StringBuilder json = new StringBuilder("{");
        for (String argument : arguments) {
            if (argument.isBlank()) {
                continue;
            }
            String[] keyValuePair = argument.split("=", 2);
            if (keyValuePair.length < 2) {
                if (keyValuePair.length == 1) {
                    json.append(keyValuePair[0]);
                }
                continue;
            }

            json.append("\"").append(keyValuePair[0]).append("\":");
            json.append(encode(keyValuePair[1])).append(",");
        }

        if (json.charAt(json.length() - 1) == ',') {
            json.deleteCharAt(json.length() - 1);
        }

        json.append('}');
        return json.toString();
    }
}