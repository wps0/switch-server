package pl.wieczorkep._switch.server.config.extractor;

import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.Properties;

public class SoundPathExtractor implements ArgumentsExtractor {
    @SneakyThrows
    @Override
    public Properties extract(@NonNull final String configString) {
        if (configString.length() == 0) {
            throw new InvalidParameterException("Arguments are not set");
        }

        Properties arguments = new Properties();
        arguments.setProperty("songPath", configString.trim());

        if (Files.notExists(Paths.get(configString))) {
            throw new FileNotFoundException("File " + configString + " does not exists.");
        }

        return arguments;
    }
}
