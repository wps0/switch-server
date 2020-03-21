package pl.wieczorkep._switch.server.core.extractor;

import java.util.Optional;
import java.util.Properties;

@FunctionalInterface
public interface ArgumentsExtractor {
    /**
     * Extracts the arguments specific to the {@link pl.wieczorkep._switch.server.core.Action.Type}
     * <br />
     *
     * @param inputString Config parameters' string.
     * @return the {@link Properties} object containing the extraction result.
     */
    public Optional<Properties> extract(String inputString);
}