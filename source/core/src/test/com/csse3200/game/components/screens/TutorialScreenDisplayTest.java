package com.csse3200.game.components.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.csse3200.game.GdxGame;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.input.InputService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ButtonSoundService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class TutorialScreenDisplayTest {
    private GdxGame mockGame;
    private TutorialScreenDisplay display;
    private Stage stage;
    private ButtonSoundService mockSoundService;

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
        mockSoundService = mock(ButtonSoundService.class);
        ServiceLocator.registerButtonSoundService(mockSoundService);

        TutorialStep step1 = new TutorialStep("Title 1", "Descition 1", null);
        TutorialStep step2 = new TutorialStep("Title 2", "Descition 2", null);

        display = new TutorialScreenDisplay(mockGame, List.of(step1, step2));
        display.create();
    }

    private Table captureRoot() {
        ArgumentCaptor<Table> cap = ArgumentCaptor.forClass(Table.class);
        verify(stage, atLeastOnce()).addActor(cap.capture());
        return cap.getValue();
    }

    private static ImageButton[] findPrevNext(Table root) {
        for (Actor a : root.getChildren()) {
            if (a instanceof Table animRow) {
                ImageButton prev = null, next = null;
                if (animRow.getChildren().size >= 2) {
                    if (animRow.getChildren().first() instanceof ImageButton ib) prev = ib;
                    if (animRow.getChildren().peek()  instanceof ImageButton ib) next = ib;
                }
                if (prev != null && next != null) return new ImageButton[]{prev, next};
            }
        }
        return new ImageButton[]{null, null};
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

    @Test
    void testShowStepWithClipLoadsAnimation() {
        TutorialClip mockClip = mock(TutorialClip.class);
        TutorialStep stepWithClip = new TutorialStep("Title", "Description", mockClip);
        TutorialScreenDisplay displayWithClip = new TutorialScreenDisplay(mockGame, List.of(stepWithClip));

        displayWithClip.create();

        assertDoesNotThrow(() -> displayWithClip.showStep(0));
    }

    @Test
    void BadClip_hitsCatch_andShowsFallback() {
        TutorialClip bad = new TutorialClip("images/does_not_exist", "frame_%04d.png", 3, 12f, true);
        TutorialStep s1 = new TutorialStep("HasBadClip", "Desc", bad);

        TutorialScreenDisplay display = new TutorialScreenDisplay(mockGame, List.of(s1));
        display.create();
        Table root = captureRoot();

        display.showStep(0);

        boolean foundFallback = false;
        for (Actor a : root.getChildren()) {
            if (a instanceof Table t) {
                for (Actor b : t.getChildren()) {
                    if (b instanceof Label l && "Demo unavailable".contentEquals(l.getText())) {
                        foundFallback = true;
                    }
                }
            }
        }
        assertTrue(foundFallback);
    }

    @Test
    void prevAndNext_buttons_playClickSound() {
        TutorialStep s1 = new TutorialStep("A", "B", null);
        TutorialStep s2 = new TutorialStep("C", "D", null);
        TutorialScreenDisplay display = new TutorialScreenDisplay(mockGame, List.of(s1, s2));

        display.create();
        Table root = captureRoot();

        // Click Next
        ImageButton[] nav = findPrevNext(root);
        assertNotNull(nav[1]);
        nav[1].fire(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent());
        verify(mockSoundService, atLeastOnce()).playClick();

        Table root2 = captureRoot();
        ImageButton[] nav2 = findPrevNext(root2);
        assertNotNull(nav2[0]);

        // Click Prev
        nav2[0].fire(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent());
        verify(mockSoundService, atLeast(2)).playClick();
    }

    @Test
    void disposeRunsWithoutException() {
        var display = new TutorialScreenDisplay(mockGame, List.of(new TutorialStep("T","D", null)));
        display.create();
        assertDoesNotThrow(display::dispose);
    }
}