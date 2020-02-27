package pl.wieczorkep._switch.server.config.extractor;

import java.util.Properties;

@FunctionalInterface
public interface ArgumentsExtractor {
    public Properties extract(String inputString);
}
