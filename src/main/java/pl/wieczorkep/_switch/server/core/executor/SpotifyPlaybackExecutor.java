package pl.wieczorkep._switch.server.core.executor;

import java.util.Properties;

public class SpotifyPlaybackExecutor implements ActionExecutor {

    @Override
    public boolean execute(Properties arguments) throws InterruptedException {
        String contextUri = (String) arguments.getOrDefault("contextUri", "");
        String deviceId = (String) arguments.getOrDefault("deviceId", "");
        int offset = Integer.parseInt((String) arguments.getOrDefault("offset", "0"));

        // STOPSHIP: 23.08.2020 Dopisać dalej start playbacku, po czym czekanie duration i zatrzymywanie playbacku jeśli
        //  po tym czasie nie został jeszcze zatrzymany
        return false;
    }
}
