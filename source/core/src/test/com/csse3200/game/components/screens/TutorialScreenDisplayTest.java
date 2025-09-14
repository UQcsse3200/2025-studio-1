package com.csse3200.game.components.screens;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.csse3200.game.GdxGame;
import org.junit.jupiter.api.*;
import com.badlogic.gdx.scenes.scene2d.Actor;

import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class TutorialScreenDisplayTest {
    private GdxGame mockGame;
    private TutorialScreenDisplay display;
    private Stage stage;

    @BeforeEach
    void createConditions() {
        ServiceLocator.registerResourceService(new ResourceService());
        ServiceLocator.getResourceService().loadAll();

        stage = mock(Stage.class);
        RenderService renderService = new RenderService();
        renderService.setStage(stage);
        ServiceLocator.registerRenderService(renderService);

        ServiceLocator.registerInputService(new InputService());

        mockGame = mock(GdxGame.class);

        TutorialStep step1 = new TutorialStep("Title 1", "Descition 1", null);
        TutorialStep step2 = new TutorialStep("Title 2", "Descition 2", null);

        display = new TutorialScreenDisplay(mockGame, List.of(step1, step2));
        display.create();
    }

    @Test
    void startsAtFirstStep() {
        Assertions.assertEquals(0, display.currentStep);
    }


    @Test
    void mainMenuButtonSwitchesScreen() {
        display.backMainMenu();
        verify(mockGame).setScreen(GdxGame.ScreenType.MAIN_MENU);
    }

    @Test
    void nextAndPreviousButtonChangesStep() {
        int initialStep = display.currentStep;
        Assertions.assertEquals(0, initialStep);

        // Simulate next button click
        display.showStep(initialStep);
        display.currentStep++;
        display.showStep(display.currentStep);

        Assertions.assertEquals(1, display.currentStep);

        // Simulate previous button click
        display.currentStep--;
        display.showStep(display.currentStep);

        Assertions.assertEquals(0, display.currentStep);
    }


    @Test
    void testCreateMethodExecutesWithoutException() {
        assertDoesNotThrow(() -> {
            TutorialStep step1 = new TutorialStep("Test Step 1", "Test Description 1", null);
            TutorialStep step2 = new TutorialStep("Test Step 2", "Test Description 2", null);
            TutorialScreenDisplay testDisplay = new TutorialScreenDisplay(mockGame, List.of(step1, step2));
            testDisplay.create();
        });
    }

    @Test
    void testLabelAndButtonCreatedCorrectly() {
        ArgumentCaptor<Table> tableCaptor = ArgumentCaptor.forClass(Table.class);
        verify(stage, atLeastOnce()).addActor(tableCaptor.capture());

        Table rootTable = tableCaptor.getValue();
        assertNotNull(rootTable);

        String expectedTitle = "Title 1";
        String expectedDescription = "Descition 1";
        boolean hasTitle = false;
        boolean hasDescription = false;
        boolean hasMainMenuButton = false;
        boolean hasNextButton = false;
        boolean hasPrevButton = false;

        for (Actor actor : rootTable.getChildren()) {
            if (actor instanceof Label label) {
                String text = label.getText().toString();
                if (text.equals(expectedTitle)) hasTitle = true;
                if (text.equals(expectedDescription)) hasDescription = true;
            } else if (actor instanceof TextButton button) {
                if (button.getText().toString().equals("Main Menu")) {
                    hasMainMenuButton = true;
                    break;
                }
            } else if (actor instanceof Table animTable) {
                for (Actor nestedActor : animTable.getChildren()) {
                    if (nestedActor instanceof ImageButton) {
                        int index = animTable.getChildren().indexOf(nestedActor, true);
                        if (index == 0) hasPrevButton = true;
                        if (index == animTable.getChildren().size - 1) hasNextButton = true;
                    }
                }
            }
        }
        assertTrue(hasTitle);
        assertTrue(hasDescription);
        assertTrue(hasMainMenuButton);
        assertTrue(hasPrevButton);
        assertTrue(hasNextButton);
    }


}