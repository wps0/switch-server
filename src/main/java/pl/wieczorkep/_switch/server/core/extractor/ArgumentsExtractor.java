package pl.wieczorkep._switch.server.core.extractor;

import java.util.Optional;
import java.util.Properties;

@FunctionalInterface
public interface ArgumentsExtractor {
    /**
     * Extracts the arguments specific to the {@link pl.wieczorkep._switch.server.core.Action.Type Type}
     * <br />
     *
     * @param inputString Config parameters' string.
     * @return {@link Properties} object containing the extraction result.
     */
    public Optional<Properties> extract(String inputString);
}
