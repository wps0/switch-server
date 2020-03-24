package pl.wieczorkep._switch.server.core.utils;

import org.junit.jupiter.api.*;
import pl.wieczorkep._switch.server.core.AppConfig;
import pl.wieczorkep._switch.server.view.ConsoleView;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ConfigUtilsTest {

    private AppConfig appConfig;

    @BeforeEach
    void initEach() {
        appConfig = new AppConfig(new ConsoleView());
        appConfig.init();
    }

    @Test
    @DisplayName("initializeConfig(): should throw NullPointerException when AppConfig parameter is null")
    void initializeConfig_nullAppConfig() {
        // given
        appConfig = null;

        // when
        // then
        assertThrows(NullPointerException.class, () -> {
            ConfigUtils.initializeConfig(appConfig);
        });
    }

    @Test
    @DisplayName("should throw NullPointerException when AppConfig parameter is null")
    void initializeConfig_filesNotExists() {
        // given

        // when
        when(appConfig.get(AppConfig.CONFIG_DIR)).thenReturn(System.getProperty("user.home") + File.separatorChar + "SwitchSoundServer");
        when(appConfig.get(AppConfig.CONFIG_FILE)).thenReturn(
                System.getProperty("user.home") + File.separatorChar + "SwitchSoundServer" + File.separatorChar + "config.props"
        );

        // then
        assertThrows(NullPointerException.class, () -> {
            ConfigUtils.initializeConfig(appConfig);
        });
    }


    @Test
    @Disabled
    void loadConfig() {
        fail("not yet implemented");
    }
}
