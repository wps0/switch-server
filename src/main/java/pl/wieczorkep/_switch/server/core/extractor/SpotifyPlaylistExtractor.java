package pl.wieczorkep._switch.server.core.extractor;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;
import java.util.Properties;

public class SpotifyPlaylistExtractor implements ArgumentsExtractor {

    @Override
    public Optional<Properties> extract(final String inputString) {
        if (inputString == null || inputString.length() == 0) {
            return Optional.empty();
        }

        Properties arguments = new Properties();
        try {
            arguments.load(new StringReader(inputString));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.of(arguments);
    }
}
