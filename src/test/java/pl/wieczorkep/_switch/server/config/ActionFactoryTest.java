package pl.wieczorkep._switch.server.config;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ActionFactoryTest {
    private ActionFactory actionFactory;


    @BeforeEach
    void setUp() {
        this.actionFactory = new ActionFactory();
    }

    @Test
    void createExampleAction() {
        // when
        Action exampleAction = actionFactory.createExampleAction();

        // then
        assertNotNull(exampleAction.getActionId(), "Action ID should not be null");
        assertNotNull(exampleAction.getType(), "Action Type should not be null");
        assertNotNull(exampleAction.getExecutionTime(), "Execution time should not be null");
        assertNotNull(exampleAction.getTypeArguments(), "Type arguments should not be null");
        assertNotNull(exampleAction.getExecutionTime().getExecutionDays(), "Execution days should not be null");
    }

    @Test
    @DisplayName("should throw IOException when cannot create action's file")
    @Disabled
    void createActionFile_fromActionAndFile_shouldThrowIOException(@TempDir File container) {
        // given
        Action exampleAction = new Action(
                new Action.ExecutionTime((byte) 10, (byte) 20, new DayOfWeek[]{DayOfWeek.TUESDAY}),
                Action.Type.PLAY_SOUND,
                null,
                "testExampleAction.action"
        );
        File actionsDir = new File(container, "actionsDir");
        System.out.println(actionsDir.mkdir());

        // when
        // ToDo: set the directory to read only
        System.out.println(actionsDir.setWritable(true, true));

        // then
        assertThrows(IOException.class, () ->
                actionFactory.createActionFile(exampleAction, actionsDir)
        );
    }

    @Test
    @DisplayName("should create action file from given action")
    void createActionFile_fromActionAndFile_shouldCreateFileFromGivenAction(@TempDir File container) throws IOException {
        // given
        Action exampleAction = new Action(
                new Action.ExecutionTime((byte) 10, (byte) 20, new DayOfWeek[]{DayOfWeek.TUESDAY}),
                Action.Type.PLAY_SOUND,
                null,
                "testExampleAction.action"
        );

        // when
        Optional<File> actionFileOptional = actionFactory.createActionFile(exampleAction, container);
        File actionFile = actionFileOptional.orElse(null);

        // then
        assertNotNull(actionFile, "Should create action file");
        assertNotEquals(0, actionFile.length(), "Action file should not be empty");
        assertNotEquals(container, actionFile);
    }
}
