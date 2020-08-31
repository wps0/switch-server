package pl.wieczorkep._switch.server.core.executor;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import pl.wieczorkep._switch.server.SoundServer;
import pl.wieczorkep._switch.server.core.Action;
import pl.wieczorkep._switch.server.integration.spotify.SpotifyApiGateway;
import pl.wieczorkep._switch.server.integration.spotify.SpotifyMacros;

import java.net.http.HttpResponse;
import java.util.Properties;

@Log4j2
public class SpotifyPlaybackExecutor implements IActionExecutor {
    @Setter
    private Action action;

    @Override
    public boolean execute(SoundServer server) throws InterruptedException {
        Properties props = action.getArguments();
        // start playback variables
        String contextUri = props.getProperty("contextUri", "");
        String deviceId = props.getProperty("deviceId", "");
        int offset = Integer.parseInt(props.getProperty("offset", "0"));
        SpotifyApiGateway apiGateway = server.getSpotifyApiGateway();
        // duration of an action; spotify specific
        int duration = Integer.parseInt(action.getArguments().getProperty("duration")) * 60 - 15;

        HttpResponse<String> startResponse = SpotifyMacros.Player.startPlayback(apiGateway, deviceId, contextUri, offset, 0);
        HttpResponse<String> startVolumeResponse = SpotifyMacros.Player.changeVolume(apiGateway, deviceId, 75);
        if (startResponse.statusCode() != 204) {
            LOGGER.warn("Spotify startPlayback returned code " + startResponse.statusCode());
            LOGGER.warn("Spotify startPlayback returned response: " + startResponse.body());
        }
        if (startResponse.statusCode() == 404) {
            LOGGER.warn("Target device not found. Action will be cancelled.");
            return false;
        }
        if (startVolumeResponse.statusCode() != 204) {
            LOGGER.warn("Spotify changeVolume returned code " + startVolumeResponse.statusCode());
            LOGGER.warn("Spotify changeVolume returned response " + startVolumeResponse.body());
        }

        int stopCount = duration / 5;
        while (stopCount != 0) {
            synchronized (this) {
                wait(5000);
            }
            stopCount--;
        }

        // TODO: better edge case handling
        boolean playbackStopped = false;
        while (!playbackStopped) {
            HttpResponse<String> pauseResponse = SpotifyMacros.Player.pausePlayback(apiGateway, deviceId);
            if (pauseResponse.statusCode() != 204) {
                if (startResponse.statusCode() == 404) {
                    LOGGER.warn("Target device not found. Action will be cancelled.");
                    break;
                }
                synchronized (this) {
                    wait(1250);
                }
                HttpResponse<String> volumeResponse = SpotifyMacros.Player.changeVolume(apiGateway, deviceId, 0);
                if (volumeResponse.statusCode() == 204) {
                    break;
                }
            } else {
                playbackStopped = true;
            }
        }

        return true;
    }
}
