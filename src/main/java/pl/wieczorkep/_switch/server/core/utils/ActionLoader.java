package pl.wieczorkep._switch.server.core.utils;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import pl.wieczorkep._switch.server.core.Action;
import pl.wieczorkep._switch.server.core.AppConfig;

import java.io.*;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pl.wieczorkep._switch.server.Constants.*;

@Log4j2
public class ActionLoader {
    private ActionLoader() {}

    public static void loadActions(AppConfig appConfig) throws IOException {
        LOGGER.info("Loading actions...");
        @Cleanup
        BufferedInputStream actionsInputStream = new BufferedInputStream(new FileInputStream(appConfig.get(ACTIONS_FILE)));

        Properties activeActions = new Properties();
        activeActions.load(actionsInputStream);

        String actions = (String) Optional.ofNullable(activeActions.get("active"))
                .orElse("");

        appConfig.putActions(Arrays.stream(actions.split(","))
                .filter(actionId -> actionId.length() > 0)
                .map(actionId -> actionId.trim() + ".action")
                .distinct()
                .map(actionFile -> loadAction(new File(appConfig.get(ACTIONS_DIR) + File.separatorChar + actionFile)))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
    }

    @SneakyThrows
    public static Action loadAction(File actionFile) {
        FileReader reader = new FileReader(actionFile);
        StringBuilder fileStringBuilder = new StringBuilder();

        int character;
        while ((character = reader.read()) != -1) {
            fileStringBuilder.append((char) character);
        }

        String fileContents = fileStringBuilder.toString().replace("\\", "\\\\");
        Properties actionProperties = new Properties();
        actionProperties.load(new StringReader(fileContents));

        try {
            return new Action(
                    Byte.parseByte(actionProperties.getProperty("hour", "0")),
                    Byte.parseByte(actionProperties.getProperty("minute", "0")),
                    Stream.of(actionProperties
                            .getProperty("days", "")
                            .replace('{', ' ')
                            .replace('}', ' ')
                            .split(","))
                            .map(String::trim)
                            .map(ActionUtils::decodeDay)
                            .distinct()
                            .toArray(DayOfWeek[]::new),
                    Action.Type.valueOf(actionProperties.getProperty("type", "PLAY_SOUND")),
                    actionProperties.getProperty("typeArguments"),
                    actionFile.getName()
            );
        } catch (Exception e) {
            LOGGER.error("Could not load " + actionFile.getName() + "; " + e.getLocalizedMessage());
            LOGGER.error(e);
        }
        return null;
    }

}
