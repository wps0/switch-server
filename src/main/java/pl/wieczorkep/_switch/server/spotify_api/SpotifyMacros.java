package pl.wieczorkep._switch.server.spotify_api;

import com.google.gson.Gson;
import pl.wieczorkep._switch.server.spotify_api.model.CurrentlyPlaying;
import pl.wieczorkep._switch.server.spotify_api.model.Devices;

import java.net.URI;
import java.net.http.HttpResponse;

import static pl.wieczorkep._switch.server.spotify_api.SpotifyApiGateway.AuthMethod.BEARER;
import static pl.wieczorkep._switch.server.spotify_api.SpotifyApiGateway.RequestMethod.*;

public class SpotifyMacros {
    private static Gson gson = new Gson();
    private SpotifyMacros() {}

    public static class Player {
        public static String API_PREFIX = SpotifyApiGateway.API_ENDPOINT + "/me/player";
        private Player() {}

        /**
         * @param apiGateway Spotify API gateway object.
         * @see <a href="https://developer.spotify.com/documentation/web-api/reference/player/get-information-about-the-users-current-playback/">Spotify API reference</a>
         */
        public static CurrentlyPlaying getPlaybackInfo(SpotifyApiGateway apiGateway) {
            return decode(apiGateway.makeRequest(URI.create(API_PREFIX),
                    "",
                    GET,
                    BEARER).body(), CurrentlyPlaying.class);
        }

        /**
         * @param apiGateway Spotify API gateway object.
         * @see <a href="https://developer.spotify.com/documentation/web-api/reference/player/get-a-users-available-devices/">Spotify API reference</a>
         */
        public static Devices getAvailableDevices(SpotifyApiGateway apiGateway) {
            return decode(apiGateway.makeRequest(URI.create(API_PREFIX + "/devices"),
                    "",
                    GET,
                    BEARER).body(), Devices.class);
        }

        /**
         * Pause a user's playback
         *
         * @param apiGateway Spotify API gateway object.
         * @param deviceId Optional. The id of the device this command is targeting. If not supplied, the user’s
         *                 currently active device is the target.
         * @return True if the status code is equals to 204, false otherwise.
         */
        public static boolean pausePlayback(SpotifyApiGateway apiGateway, String deviceId) {
            HttpResponse<String> response = apiGateway.makeRequest(URI.create(API_PREFIX + "/pause"), deviceId, PUT, BEARER);
            return response.statusCode() == 204;
        }

        /**
         * Pause a user's playback
         *
         * @param apiGateway Spotify API gateway object.
         * @param deviceId Optional. The id of the device this command is targeting. If not supplied, the user’s
         *                 currently active device is the target.
         * @param volume Volume in range 0-100. If volume is out of range, it's rounded to 100 (if volume > 100)
         *               or 0 (if volume < 0).
         * @return True if the status code is equals to 204, false otherwise.
         */
        public static boolean changeVolume(SpotifyApiGateway apiGateway, String deviceId, int volume) {
            if (volume > 100) {
                volume = 100;
            } else if (volume < 0) {
                volume = 0;
            }

            HttpResponse<String> response = apiGateway.makeRequest(URI.create(API_PREFIX + "/volume?volume_percent=" + volume + (!deviceId.isBlank() ? "&device_id=" + deviceId : "")),
                    "",
                    PUT,
                    BEARER);
            return response.statusCode() == 204;
        }
    }

    private static <T> T decode(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }

    /**
     * @param params Every even index is argument's name, each odd is argument's value.
     *               Eg. {"device_id", "abc123", "volume", "10"} makes json {"device_id": abc123, "volume": 10}
     * @return
     */
//    private static String encode(String... params) {
//        if ((params.length & 1) == 1) {
//            throw new IllegalArgumentException("amount of arguments has to be even");
//        }
//
//        StringBuilder json = new StringBuilder();
//        json.append('{');
//        for (int i = 0; i < params.length; i++) {
//            if ((i & 1) == 0) {
//                if (params[i].isBlank()) {
//                    i++;
//                    continue;
//                }
//                json.append("\"").append(params[i]).append("\":");
//            } else {
//                json.append(params[i]).append(",");
//            }
//        }
//        json.deleteCharAt(json.length() - 1);
//        json.append('}');
//        return json.toString();
//    }
}
