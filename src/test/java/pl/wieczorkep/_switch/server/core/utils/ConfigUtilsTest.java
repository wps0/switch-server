package pl.wieczorkep._switch.server.core.utils;

import org.junit.jupiter.api.*;
import pl.wieczorkep._switch.server.core.AppConfig;

import java.io.File;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;
import static pl.wieczorkep._switch.server.Constants.*;

class ConfigUtilsTest {

    private AppConfig appConfig;

    @BeforeEach
    void initEach() {
        appConfig = new AppConfig();
        appConfig.init();
    }

    @Test
    @Disabled
    @DisplayName("initializeConfig(): should throw NullPointerException when AppConfig parameter is null")
    void initializeConfig_nullAppConfig() {
        // given
        appConfig = null;
        // when
        // then
        /*assertThrows(NullPointerException.class, () -> {
            ConfigUtils.initializeConfig(appConfig);
        });*/
    }

    @Test
    @Disabled
    @DisplayName("should throw NullPointerException when AppConfig parameter is null")
    void initializeConfig_filesNotExists() {
        // given

        // when
        when(appConfig.get(CONFIG_DIR)).thenReturn(System.getProperty("user.home") + File.separatorChar + "SwitchSoundServer");
        when(appConfig.get(CONFIG_FILE)).thenReturn(
                System.getProperty("user.home") + File.separatorChar + "SwitchSoundServer" + File.separatorChar + "config.props"
        );

        // then
        /*assertThrows(NullPointerException.class, () -> {
            ConfigUtils.initializeConfig(appConfig);
        });*/
    }


    @Test
    @Disabled
    void loadConfig() {
        fail("not yet implemented");
    }
}
